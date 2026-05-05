package com.healthcare.backend.service.impl;

import com.healthcare.backend.dto.request.CreateAppointmentRequest;
import com.healthcare.backend.dto.request.CreateWalkInAppointmentRequest;
import com.healthcare.backend.dto.request.SepayWebhookRequest;
import com.healthcare.backend.dto.response.AppointmentResponse;
import com.healthcare.backend.entity.Account;
import com.healthcare.backend.entity.Appointment;
import com.healthcare.backend.entity.ConsultationFee;
import com.healthcare.backend.entity.DoctorSchedule;
import com.healthcare.backend.entity.Patient;
import com.healthcare.backend.entity.PaymentRecord;
import com.healthcare.backend.entity.PaymentTransaction;
import com.healthcare.backend.entity.enums.AppointmentStatus;
import com.healthcare.backend.entity.enums.PaymentStatus;
import com.healthcare.backend.entity.enums.PaymentTransactionStatus;
import com.healthcare.backend.exception.BusinessException;
import com.healthcare.backend.exception.ResourceNotFoundException;
import com.healthcare.backend.mapper.AppointmentMapper;
import com.healthcare.backend.repository.AccountRepository;
import com.healthcare.backend.repository.AppointmentRepository;
import com.healthcare.backend.repository.ConsultationFeeRepository;
import com.healthcare.backend.repository.DoctorScheduleRepository;
import com.healthcare.backend.repository.PatientRepository;
import com.healthcare.backend.repository.PaymentRecordRepository;
import com.healthcare.backend.repository.PaymentTransactionRepository;
import com.healthcare.backend.security.UserPrincipal;
import com.healthcare.backend.service.AppointmentService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private static final Pattern APPOINTMENT_CODE_PATTERN = Pattern.compile("APT-[A-Z0-9]{8}");
    private static final long ONLINE_PAYMENT_RESERVATION_MINUTES = 10;
    private static final Set<AppointmentStatus> ACTIVE_STATUSES = EnumSet.of(
            AppointmentStatus.PENDING,
            AppointmentStatus.CONFIRMED,
            AppointmentStatus.CHECKED_IN,
            AppointmentStatus.IN_PROGRESS
    );

    private final AppointmentRepository appointmentRepository;
    private final AccountRepository accountRepository;
    private final ConsultationFeeRepository consultationFeeRepository;
    private final PatientRepository patientRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final AppointmentMapper appointmentMapper;
    private final EntityManager entityManager;

    @Value("${sepay.webhook-secret:}")
    private String sepayWebhookSecret;

    @Value("${sepay.account-number:}")
    private String sepayAccountNumber;

    @Override
    // Transaction gom thao tác giữ slot, tạo appointment và tạo payment record thành một đơn vị atomic.
    // Nếu bất kỳ bước nào lỗi thì toàn bộ booking được rollback, tránh giữ slot trống.
    @Transactional
    public AppointmentResponse create(CreateAppointmentRequest request) {
        Patient patient = findPatientForUpdateOrThrow(request.getPatientId());
        DoctorSchedule doctorSchedule = findDoctorScheduleForUpdateOrThrow(request.getDoctorScheduleId());
        ConsultationFee consultationFee = findConsultationFeeByScheduleOrThrow(doctorSchedule);

        validateScheduleNotExpired(doctorSchedule);
        validateNoActiveAppointment(patient.getPatientId());
        validateConsultationFeeActive(consultationFee);
        validateDoctorScheduleCapacity(doctorSchedule);

        int nextQueueNumber = reserveDoctorScheduleSlot(doctorSchedule);

        Appointment appointment = appointmentMapper.toEntity(request);
        populateAppointmentForBooking(appointment, patient, doctorSchedule, consultationFee);
        appointment.setQueueNum(nextQueueNumber);
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setPaymentExpiresAt(LocalDateTime.now().plusMinutes(ONLINE_PAYMENT_RESERVATION_MINUTES));

        Appointment savedAppointment = appointmentRepository.saveAndFlush(appointment);
        initializePaymentRecord(savedAppointment);
        entityManager.refresh(savedAppointment);

        return appointmentMapper.toResponse(savedAppointment);
    }

    @Override
    // Transaction đảm bảo flow walk-in (khám trực tiếp): thu tiền, tạo appointment, payment record và cash transaction cùng commit.
    // Nếu lưu giao dịch tiền mặt thất bại thì appointment cũng không được tạo lệch trạng thái.
    @Transactional
    public AppointmentResponse createWalkInPaidAppointment(CreateWalkInAppointmentRequest request) {
        Account currentAccount = findCurrentAccountOrThrow();
        Patient patient = findPatientForUpdateOrThrow(request.getPatientId());
        DoctorSchedule doctorSchedule = findDoctorScheduleForUpdateOrThrow(request.getDoctorScheduleId());
        ConsultationFee consultationFee = findConsultationFeeByScheduleOrThrow(doctorSchedule);

        validateScheduleNotExpired(doctorSchedule);
        validateNoActiveAppointment(patient.getPatientId());
        validateConsultationFeeActive(consultationFee);
        validateDoctorScheduleCapacity(doctorSchedule);
        validateExactAmount(request.getReceivedAmount(), consultationFee.getPrice());

        int nextQueueNumber = reserveDoctorScheduleSlot(doctorSchedule);

        Appointment appointment = appointmentMapper.toEntity(request);
        populateAppointmentForBooking(appointment, patient, doctorSchedule, consultationFee);
        appointment.setQueueNum(nextQueueNumber);
        appointment.setStatus(AppointmentStatus.CHECKED_IN);
        appointment.setPaidAt(LocalDateTime.now());
        appointment.setCheckedInAt(appointment.getPaidAt());

        Appointment savedAppointment = appointmentRepository.saveAndFlush(appointment);

        PaymentRecord paymentRecord = initializePaymentRecord(savedAppointment);
        paymentRecord.setReceivedAmount(request.getReceivedAmount());
        paymentRecord.setPaidAt(savedAppointment.getPaidAt());
        paymentRecord.setPaymentStatus(PaymentStatus.PAID);

        PaymentTransaction paymentTransaction = buildPaymentTransaction(
                paymentRecord,
                null,
                "CASH",
                request.getReceiptNumber(),
                request.getReceivedAmount(),
                LocalDateTime.now(),
                null,
                "cash",
                request.getNote(),
                request.getNote(),
                request.getNote(),
                currentAccount,
                request.getReceiptNumber()
        );

        paymentTransactionRepository.save(paymentTransaction);
        paymentRecordRepository.save(paymentRecord);
        entityManager.refresh(savedAppointment);

        return appointmentMapper.toResponse(savedAppointment);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getById(Long appointmentId) {
        return appointmentMapper.toResponse(findAppointmentOrThrow(appointmentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAll(Long patientId, Long doctorScheduleId, AppointmentStatus status) {
        return appointmentRepository.findAllByFilters(patientId, doctorScheduleId, status)
                .stream()
                .sorted(Comparator.comparing(Appointment::getCreatedAt).reversed())
                .map(appointmentMapper::toResponse)
                .toList();
    }

    @Override
    // Transaction + khóa appointment để check-in không chạy song song với cancel/start.
    @Transactional
    public AppointmentResponse checkIn(Long appointmentId) {
        Appointment appointment = findAppointmentForUpdateOrThrow(appointmentId);

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BusinessException("Only confirmed appointments can be checked in");
        }

        appointment.setStatus(AppointmentStatus.CHECKED_IN);
        appointment.setCheckedInAt(LocalDateTime.now());

        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Override
    // Transaction + khóa appointment để chỉ một request được chuyển lịch sang IN_PROGRESS.
    @Transactional
    public AppointmentResponse start(Long appointmentId) {
        Appointment appointment = findAppointmentForUpdateOrThrow(appointmentId);

        if (appointment.getStatus() != AppointmentStatus.CHECKED_IN) {
            throw new BusinessException("Only checked-in appointments can be started");
        }

        appointment.setStatus(AppointmentStatus.IN_PROGRESS);

        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Override
    // Transaction dùng để hủy appointment và release slot trong cùng một lần commit.
    @Transactional
    public AppointmentResponse cancel(Long appointmentId) {
        Appointment appointment = findAppointmentForUpdateOrThrow(appointmentId);

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BusinessException("Appointment is already cancelled");
        }

        if (appointment.getStatus() == AppointmentStatus.CONFIRMED
                || appointment.getStatus() == AppointmentStatus.CHECKED_IN
                || appointment.getStatus() == AppointmentStatus.IN_PROGRESS
                || appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BusinessException("Paid appointments cannot be cancelled");
        }

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new BusinessException("Only pending appointments can be cancelled");
        }

        releaseReservedDoctorScheduleSlot(appointment);
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledAt(LocalDateTime.now());

        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Override
    // Transaction dùng cho webhook SePay: validate, khóa appointment, cập nhật paid và ghi transaction cùng lúc.
    // Nhờ vậy không có trạng thái đã paid nhưng thiếu payment transaction cho kế toán.
    @Transactional
    public AppointmentResponse confirmPaymentFromSepayWebhook(SepayWebhookRequest request, String secretKeyHeader) {
        validateSepaySecret(secretKeyHeader);
        validateIncomingTransfer(request);

        if (request.getId() != null) {
            PaymentTransaction existingTransaction = paymentTransactionRepository
                    .findBySepayTransactionId(request.getId().toString())
                    .orElse(null);
            if (existingTransaction != null) {
                return appointmentMapper.toResponse(existingTransaction.getPaymentRecord().getAppointment());
            }
        }

        String appointmentCode = extractAppointmentCode(request);
        Appointment appointment = appointmentRepository.findByAppointmentCodeForUpdate(appointmentCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with code: " + appointmentCode
                ));

        if (request.getId() != null
                && appointment.getSepayTransactionId() != null
                && appointment.getSepayTransactionId().equals(request.getId())) {
            return appointmentMapper.toResponse(appointment);
        }

        return confirmPaymentInternal(
                appointment,
                request.getId(),
                defaultIfBlank(request.getGateway(), "SEPAY"),
                request.getReferenceCode(),
                toAmount(request.getTransferAmount()),
                parseTransactionDate(request.getTransactionDate()),
                request.getAccountNumber(),
                request.getTransferType(),
                request.getContent(),
                request.getDescription(),
                toRawJson(request)
        );
    }

    private Appointment findAppointmentOrThrow(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));
    }

    private Appointment findAppointmentForUpdateOrThrow(Long appointmentId) {
        return appointmentRepository.findByIdForUpdate(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));
    }

    private ConsultationFee findConsultationFeeByScheduleOrThrow(DoctorSchedule doctorSchedule) {
        if (doctorSchedule.getDoctor() == null || doctorSchedule.getDoctor().getSpecialty() == null) {
            throw new BusinessException("Doctor specialty is required to resolve consultation fee");
        }

        Long specialtyId = doctorSchedule.getDoctor().getSpecialty().getSpecialtyId();
        return consultationFeeRepository.findFirstBySpecialtyRef_SpecialtyIdAndIsActive(specialtyId, 1)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Consultation fee not found for doctor specialty id: " + specialtyId
                ));
    }

    private Patient findPatientForUpdateOrThrow(Long patientId) {
        Patient patient = patientRepository.findByIdForUpdate(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

        if (!Integer.valueOf(1).equals(patient.getIsActive())) {
            throw new ResourceNotFoundException("Patient not found with id: " + patientId);
        }

        return patient;
    }

    private DoctorSchedule findDoctorScheduleForUpdateOrThrow(Long doctorScheduleId) {
        return doctorScheduleRepository.findByIdForUpdate(doctorScheduleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor schedule not found with id: " + doctorScheduleId
                ));
    }

    private Account findCurrentAccountOrThrow() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BusinessException("Authenticated receptionist account is required");
        }

        return accountRepository.findById(principal.accountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + principal.accountId()));
    }

    private void validateNoActiveAppointment(Long patientId) {
        boolean hasActiveAppointment = appointmentRepository
                .findFirstByPatient_PatientIdAndStatusIn(patientId, ACTIVE_STATUSES)
                .isPresent();

        if (hasActiveAppointment) {
            throw new BusinessException("Patient already has an active appointment");
        }
    }

    private void validateScheduleNotExpired(DoctorSchedule doctorSchedule) {
        LocalDate scheduleDate = doctorSchedule.getScheduleDate();
        if (scheduleDate != null && scheduleDate.isBefore(LocalDate.now())) {
            throw new BusinessException("Doctor schedule has expired");
        }
    }

    private void validatePendingAppointmentForPayment(Appointment appointment) {
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            if (appointment.getStatus() == AppointmentStatus.CONFIRMED) {
                throw new BusinessException("Appointment is already confirmed");
            }
            throw new BusinessException("Only pending appointments can be confirmed for payment");
        }
    }

    private void validateDoctorScheduleCapacity(DoctorSchedule doctorSchedule) {
        if (doctorSchedule.getCurrentBookingCount() >= doctorSchedule.getMaxCapacity()) {
            throw new BusinessException("Doctor schedule is full");
        }
    }

    private int reserveDoctorScheduleSlot(DoctorSchedule doctorSchedule) {
        // Atomic increment trong transaction đang giữ khóa doctor schedule:
        // tăng booking count và cấp queue number như một thao tác không thể bị chen ngang.
        int nextQueueNumber = doctorSchedule.getLastQueueNumber() + 1;
        doctorSchedule.setLastQueueNumber(nextQueueNumber);
        doctorSchedule.setCurrentBookingCount(doctorSchedule.getCurrentBookingCount() + 1);
        return nextQueueNumber;
    }

    private void releaseReservedDoctorScheduleSlot(Appointment appointment) {
        if (appointment.getQueueNum() == null) {
            return;
        }

        DoctorSchedule doctorSchedule = findDoctorScheduleForUpdateOrThrow(
                appointment.getDoctorSchedule().getDoctorScheduleId()
        );
        // Chỉ giảm số slot đang giữ, không giảm lastQueueNumber để giữ audit trail và tránh cấp lại số cũ.
        if (doctorSchedule.getCurrentBookingCount() > 0) {
            doctorSchedule.setCurrentBookingCount(doctorSchedule.getCurrentBookingCount() - 1);
        }
    }

    private void validatePaymentReservationNotExpired(Appointment appointment) {
        if (appointment.getPaymentExpiresAt() != null
                && !appointment.getPaymentExpiresAt().isAfter(LocalDateTime.now())) {
            throw new BusinessException("Appointment payment reservation has expired");
        }
    }

    private void validateConsultationFeeActive(ConsultationFee consultationFee) {
        if (!consultationFee.isActive()) {
            throw new BusinessException("Consultation fee is inactive");
        }
    }

    private void populateAppointmentForBooking(
            Appointment appointment,
            Patient patient,
            DoctorSchedule doctorSchedule,
            ConsultationFee consultationFee
    ) {
        appointment.setPatient(patient);
        appointment.setDoctorSchedule(doctorSchedule);
        appointment.setConsultationFee(consultationFee);
        appointment.setFeeNameSnapshot(consultationFee.getFeeName());
        appointment.setFeePriceSnapshot(consultationFee.getPrice());
        appointment.setAppointmentCode(generateUniqueAppointmentCode());
    }

    private PaymentRecord initializePaymentRecord(Appointment appointment) {
        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setAppointment(appointment);
        paymentRecord.setRequestCode(appointment.getAppointmentCode());
        paymentRecord.setTotalPrice(appointment.getFeePriceSnapshot());
        paymentRecord.setReceivedAmount(BigDecimal.ZERO);
        paymentRecord.setPaymentStatus(PaymentStatus.UNPAID);
        return paymentRecordRepository.save(paymentRecord);
    }

    private PaymentRecord findPaymentRecordForUpdateOrThrow(Long appointmentId) {
        return paymentRecordRepository.findByAppointmentIdForUpdate(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment payment record not found for appointment id: " + appointmentId
                ));
    }

    private AppointmentResponse confirmPaymentInternal(
            Appointment appointment,
            Long sepayTransactionId,
            String gateway,
            String paymentReferenceCode,
            BigDecimal transferAmount,
            LocalDateTime transactionDate,
            String accountNumber,
            String transferType,
            String paymentContent,
            String description,
            String rawData
    ) {
        validatePendingAppointmentForPayment(appointment);

        // Khóa lại schedule và payment record trong cùng transaction để payment không lệch với slot đã giữ.
        DoctorSchedule doctorSchedule = findDoctorScheduleForUpdateOrThrow(appointment.getDoctorSchedule().getDoctorScheduleId());
        PaymentRecord paymentRecord = findPaymentRecordForUpdateOrThrow(appointment.getAppointmentId());

        // Refresh để lấy trạng thái mới nhất sau khi đã lấy đủ khóa liên quan.
        entityManager.refresh(appointment);

        if (sepayTransactionId != null
                && appointment.getSepayTransactionId() != null
                && appointment.getSepayTransactionId().equals(sepayTransactionId)) {
            return appointmentMapper.toResponse(appointment);
        }

        validatePendingAppointmentForPayment(appointment);
        validatePaymentReservationNotExpired(appointment);
        validatePaymentAmount(paymentRecord.getTotalPrice(), transferAmount);
        validateScheduleNotExpired(doctorSchedule);

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setPaidAt(LocalDateTime.now());
        appointment.setCancelledAt(null);
        appointment.setSepayTransactionId(sepayTransactionId);
        appointment.setPaymentReferenceCode(paymentReferenceCode);
        appointment.setPaymentContent(paymentContent);

        paymentRecord.setReceivedAmount(transferAmount);
        paymentRecord.setPaidAt(appointment.getPaidAt());
        paymentRecord.setPaymentStatus(resolvePaymentStatus(paymentRecord.getTotalPrice(), transferAmount));

        PaymentTransaction paymentTransaction = buildPaymentTransaction(
                paymentRecord,
                sepayTransactionId,
                gateway,
                paymentReferenceCode,
                transferAmount,
                transactionDate,
                accountNumber,
                transferType,
                paymentContent,
                description,
                rawData,
                null,
                null
        );

        try {
            // saveAndFlush giúp phát hiện sớm conflict unique SePay transaction trong transaction hiện tại.
            paymentTransactionRepository.save(paymentTransaction);
            paymentRecordRepository.save(paymentRecord);
            appointmentRepository.saveAndFlush(appointment);
            entityManager.refresh(appointment);
            return appointmentMapper.toResponse(appointment);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException("Payment confirmation conflict detected");
        }
    }

    @Override
    // Transaction cho scheduler: khóa các appointment hết hạn, release slot và đổi trạng thái cùng một lần commit.
    @Transactional
    public void expirePendingPaymentReservations() {
        List<Appointment> expiredAppointments = appointmentRepository.findExpiredPaymentReservationsForUpdate(
                AppointmentStatus.PENDING,
                LocalDateTime.now()
        );

        for (Appointment appointment : expiredAppointments) {
            releaseReservedDoctorScheduleSlot(appointment);
            appointment.setStatus(AppointmentStatus.PAYMENT_EXPIRED);
            appointment.setCancelledAt(LocalDateTime.now());
        }
    }

    private void validatePaymentAmount(BigDecimal expectedAmount, BigDecimal transferAmount) {
        if (transferAmount == null) {
            throw new BusinessException("Transfer amount is required");
        }

        if (expectedAmount.compareTo(transferAmount) != 0) {
            throw new BusinessException(
                    "Transfer amount does not match expected appointment amount: " + expectedAmount
            );
        }
    }

    private void validateExactAmount(BigDecimal receivedAmount, BigDecimal expectedAmount) {
        if (receivedAmount == null) {
            throw new BusinessException("Received amount is required");
        }

        if (receivedAmount.compareTo(expectedAmount) != 0) {
            throw new BusinessException("Received amount must match consultation fee amount");
        }
    }

    private PaymentStatus resolvePaymentStatus(BigDecimal expectedAmount, BigDecimal receivedAmount) {
        return receivedAmount.compareTo(expectedAmount) < 0
                ? PaymentStatus.PARTIAL
                : PaymentStatus.PAID;
    }

    private PaymentTransaction buildPaymentTransaction(
            PaymentRecord paymentRecord,
            Long sepayTransactionId,
            String gateway,
            String paymentReferenceCode,
            BigDecimal transferAmount,
            LocalDateTime transactionDate,
            String accountNumber,
            String transferType,
            String paymentContent,
            String description,
            String rawData,
            Account confirmedBy,
            String receiptNumber
    ) {
        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setPaymentRecord(paymentRecord);
        paymentTransaction.setSepayTransactionId(sepayTransactionId != null ? sepayTransactionId.toString() : null);
        paymentTransaction.setGateway(defaultIfBlank(gateway, "UNKNOWN"));
        paymentTransaction.setReferenceCode(paymentReferenceCode);
        paymentTransaction.setTransferAmount(transferAmount);
        paymentTransaction.setTransactionDate(transactionDate != null ? transactionDate : LocalDateTime.now());
        paymentTransaction.setAccountNumber(accountNumber);
        paymentTransaction.setTransferType(transferType);
        paymentTransaction.setDescription(description);
        paymentTransaction.setContent(paymentContent);
        paymentTransaction.setRawData(rawData);
        paymentTransaction.setConfirmedBy(confirmedBy);
        paymentTransaction.setReceiptNumber(receiptNumber);
        paymentTransaction.setProcessStatus(PaymentTransactionStatus.SUCCESS);
        return paymentTransaction;
    }

    private void validateSepaySecret(String secretKeyHeader) {
        if (sepayWebhookSecret == null || sepayWebhookSecret.isBlank()) {
            throw new BusinessException("SePay webhook secret is not configured");
        }

        if (!sepayWebhookSecret.equals(secretKeyHeader)) {
            throw new BusinessException("Invalid SePay webhook secret");
        }
    }

    private void validateIncomingTransfer(SepayWebhookRequest request) {
        if (request == null) {
            throw new BusinessException("SePay webhook payload must not be null");
        }

        if (!"in".equalsIgnoreCase(request.getTransferType())) {
            throw new BusinessException("Only incoming transfers can confirm appointments");
        }

        if (sepayAccountNumber != null
                && !sepayAccountNumber.isBlank()
                && !sepayAccountNumber.equals(request.getAccountNumber())) {
            throw new BusinessException("Webhook account number does not match configured SePay account");
        }

        if (request.getTransferAmount() == null || request.getTransferAmount() <= 0) {
            throw new BusinessException("Webhook transfer amount must be greater than zero");
        }
    }

    private String extractAppointmentCode(SepayWebhookRequest request) {
        if (request.getCode() != null && !request.getCode().isBlank()) {
            return request.getCode().trim().toUpperCase();
        }

        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new BusinessException("Appointment code was not found in SePay webhook payload");
        }

        Matcher matcher = APPOINTMENT_CODE_PATTERN.matcher(request.getContent().toUpperCase());
        if (!matcher.find()) {
            throw new BusinessException("Appointment code was not found in transfer content");
        }

        return matcher.group();
    }

    private String generateUniqueAppointmentCode() {
        for (int attempt = 0; attempt < 10; attempt++) {
            String candidate = "APT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
            if (!appointmentRepository.existsByAppointmentCode(candidate)) {
                return candidate;
            }
        }

        throw new BusinessException("Unable to generate unique appointment code");
    }

    private BigDecimal toAmount(Long amount) {
        return amount == null ? null : BigDecimal.valueOf(amount);
    }

    private LocalDateTime parseTransactionDate(String transactionDate) {
        if (transactionDate == null || transactionDate.isBlank()) {
            return null;
        }

        try {
            return OffsetDateTime.parse(transactionDate).toLocalDateTime();
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private String toRawJson(SepayWebhookRequest request) {
        return "{"
                + "\"id\":" + quoteNumber(request.getId()) + ","
                + "\"gateway\":" + quote(request.getGateway()) + ","
                + "\"transactionDate\":" + quote(request.getTransactionDate()) + ","
                + "\"accountNumber\":" + quote(request.getAccountNumber()) + ","
                + "\"code\":" + quote(request.getCode()) + ","
                + "\"content\":" + quote(request.getContent()) + ","
                + "\"transferType\":" + quote(request.getTransferType()) + ","
                + "\"transferAmount\":" + quoteNumber(request.getTransferAmount()) + ","
                + "\"accumulated\":" + quoteNumber(request.getAccumulated()) + ","
                + "\"subAccount\":" + quote(request.getSubAccount()) + ","
                + "\"referenceCode\":" + quote(request.getReferenceCode()) + ","
                + "\"description\":" + quote(request.getDescription())
                + "}";
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String quote(String value) {
        if (value == null) {
            return "null";
        }
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private String quoteNumber(Long value) {
        return value == null ? "null" : value.toString();
    }
}
