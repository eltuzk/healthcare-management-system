package com.healthcare.backend.service.impl;

import com.healthcare.backend.dto.request.RecordMedicalRecordPaymentRequest;
import com.healthcare.backend.dto.response.PaymentRecordResponse;
import com.healthcare.backend.entity.Account;
import com.healthcare.backend.entity.MedicalRecord;
import com.healthcare.backend.entity.PaymentRecord;
import com.healthcare.backend.entity.PaymentTransaction;
import com.healthcare.backend.entity.enums.MedicalRecordStatus;
import com.healthcare.backend.entity.enums.PaymentStatus;
import com.healthcare.backend.entity.enums.PaymentTransactionStatus;
import com.healthcare.backend.exception.BusinessException;
import com.healthcare.backend.exception.ResourceNotFoundException;
import com.healthcare.backend.mapper.PaymentRecordMapper;
import com.healthcare.backend.repository.MedicalRecordRepository;
import com.healthcare.backend.repository.PaymentRecordRepository;
import com.healthcare.backend.repository.PaymentTransactionRepository;
import com.healthcare.backend.repository.PatientRepository;
import com.healthcare.backend.repository.AccountRepository;
import com.healthcare.backend.security.UserPrincipal;
import com.healthcare.backend.service.MedicalRecordBillingService;
import com.healthcare.backend.service.PaymentRecordService;
import com.healthcare.backend.repository.LabTestRequestRepository;
import com.healthcare.backend.repository.MedicalServiceRequestRepository;
import com.healthcare.backend.entity.LabTestRequest;
import com.healthcare.backend.entity.MedicalServiceRequest;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentRecordServiceImpl implements PaymentRecordService {

    private final PaymentRecordRepository paymentRecordRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final AccountRepository accountRepository;
    private final PatientRepository patientRepository;
    private final MedicalRecordBillingService medicalRecordBillingService;
    private final PaymentRecordMapper paymentRecordMapper;
    private final LabTestRequestRepository labTestRequestRepository;
    private final MedicalServiceRequestRepository medicalServiceRequestRepository;
    private final EntityManager entityManager;

    @Value("${sepay.webhook-secret:}")
    private String sepayWebhookSecret;

    @Value("${sepay.account-number:}")
    private String sepayAccountNumber;

    @Override
    // readOnly giúp Hibernate không dirty-check không cần thiết; API payment record chỉ phục vụ xem dữ liệu kế toán.
    @Transactional(readOnly = true)
    public List<PaymentRecordResponse> getAll(PaymentStatus paymentStatus, Long appointmentId, Long medicalRecordId) {
        return paymentRecordRepository.findAllByFilters(paymentStatus, appointmentId, medicalRecordId)
                .stream()
                .filter(pr -> {
                    if (isCurrentUserPatient()) {
                        Long currentPatientId = findCurrentPatientOrThrow().getPatientId();
                        if (pr.getMedicalRecord() != null && pr.getMedicalRecord().getPatient() != null) {
                            return pr.getMedicalRecord().getPatient().getPatientId().equals(currentPatientId);
                        }
                        if (pr.getAppointment() != null && pr.getAppointment().getPatient() != null) {
                            return pr.getAppointment().getPatient().getPatientId().equals(currentPatientId);
                        }
                        return false;
                    }
                    return true;
                })
                .sorted(Comparator.comparing(PaymentRecord::getCreatedAt).reversed())
                .map(this::toResponseWithTransactions)
                .toList();
    }

    @Override
    // readOnly vì payment record/transaction được sinh bởi business flow, không sửa trực tiếp từ API này.
    @Transactional(readOnly = true)
    public PaymentRecordResponse getById(Long paymentRecordId) {
        PaymentRecord paymentRecord = paymentRecordRepository.findById(paymentRecordId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment record not found with id: " + paymentRecordId
                ));

        if (isCurrentUserPatient()) {
            Long currentPatientId = findCurrentPatientOrThrow().getPatientId();
            boolean isOwner = false;
            if (paymentRecord.getMedicalRecord() != null && paymentRecord.getMedicalRecord().getPatient() != null) {
                isOwner = paymentRecord.getMedicalRecord().getPatient().getPatientId().equals(currentPatientId);
            } else if (paymentRecord.getAppointment() != null && paymentRecord.getAppointment().getPatient() != null) {
                isOwner = paymentRecord.getAppointment().getPatient().getPatientId().equals(currentPatientId);
            }
            if (!isOwner) {
                throw new BusinessException("Patient is not allowed to access this payment record");
            }
        }

        return toResponseWithTransactions(paymentRecord);
    }

    @Override
    @Transactional
    public PaymentRecordResponse recordMedicalRecordCashPayment(
            Long medicalRecordId,
            RecordMedicalRecordPaymentRequest request
    ) {
        Account currentAccount = findCurrentAccountOrThrow();
        MedicalRecord medicalRecord = medicalRecordRepository.findByIdForUpdate(medicalRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with id: " + medicalRecordId));

        if (medicalRecord.getStatus() != MedicalRecordStatus.DRAFT) {
            throw new BusinessException("Only draft medical records can be paid and moved to IN_PROGRESS");
        }

        medicalRecordBillingService.syncBilling(medicalRecordId);

        PaymentRecord paymentRecord = paymentRecordRepository.findByMedicalRecordIdForUpdate(medicalRecordId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Medical record payment record not found for id: " + medicalRecordId
                ));

        validateExactAmount(request.getReceivedAmount(), paymentRecord.getTotalPrice());

        paymentRecord.setReceivedAmount(request.getReceivedAmount());
        paymentRecord.setPaidAt(LocalDateTime.now());
        paymentRecord.setPaymentStatus(PaymentStatus.PAID);

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setPaymentRecord(paymentRecord);
        transaction.setGateway("CASH");
        transaction.setTransferType("cash");
        transaction.setTransferAmount(request.getReceivedAmount());
        transaction.setTransactionDate(paymentRecord.getPaidAt());
        transaction.setReceiptNumber(request.getReceiptNumber());
        transaction.setContent(request.getNote());
        transaction.setDescription(request.getNote());
        transaction.setRawData(request.getNote());
        transaction.setConfirmedBy(currentAccount);
        transaction.setProcessStatus(PaymentTransactionStatus.SUCCESS);

        medicalRecord.setStatus(MedicalRecordStatus.IN_PROGRESS);
        paymentTransactionRepository.save(transaction);
        paymentRecordRepository.save(paymentRecord);
        medicalRecordRepository.save(medicalRecord);

        propagateInternalRequestPayments(medicalRecordId);

        return toResponseWithTransactions(paymentRecord);
    }

    private PaymentRecordResponse toResponseWithTransactions(PaymentRecord paymentRecord) {
        List<PaymentTransaction> transactions = paymentTransactionRepository
                .findByPaymentRecord_PaymentRecordIdOrderByTransactionDateDesc(paymentRecord.getPaymentRecordId());
        return paymentRecordMapper.toResponse(paymentRecord, transactions);
    }

    private Account findCurrentAccountOrThrow() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BusinessException("Authenticated account is required");
        }

        return accountRepository.findById(principal.accountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + principal.accountId()));
    }

    private void validateExactAmount(BigDecimal receivedAmount, BigDecimal expectedAmount) {
        if (receivedAmount == null) {
            throw new BusinessException("Received amount is required");
        }
        if (expectedAmount == null || expectedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Medical record has no payable requests");
        }
        if (receivedAmount.compareTo(expectedAmount) != 0) {
            throw new BusinessException("Received amount must match medical record total price");
        }
    }

    private boolean isCurrentUserPatient() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.getAuthorities().stream()
                        .anyMatch(authority -> "ROLE_PATIENT".equals(authority.getAuthority()));
    }

    private com.healthcare.backend.entity.Patient findCurrentPatientOrThrow() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return patientRepository.findByAccount_Email(email)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for account: " + email));
    }

    @Override
    @Transactional
    public PaymentRecordResponse confirmMedicalRecordPaymentFromSepayWebhook(
            com.healthcare.backend.dto.request.SepayWebhookRequest request,
            String secretKeyHeader
    ) {
        validateSepaySecret(secretKeyHeader);
        validateIncomingTransfer(request);

        if (request.getId() != null) {
            PaymentTransaction existingTransaction = paymentTransactionRepository
                    .findBySepayTransactionId(request.getId().toString())
                    .orElse(null);
            if (existingTransaction != null) {
                return toResponseWithTransactions(existingTransaction.getPaymentRecord());
            }
        }

        String code = null;
        if (request.getContent() != null && !request.getContent().isBlank()) {
            String contentUpper = request.getContent().toUpperCase();
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("MR-\\d+").matcher(contentUpper);
            if (matcher.find()) {
                code = matcher.group();
            }
        }
        if (code == null && request.getDescription() != null && !request.getDescription().isBlank()) {
            String descUpper = request.getDescription().toUpperCase();
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("MR-\\d+").matcher(descUpper);
            if (matcher.find()) {
                code = matcher.group();
            }
        }
        if (code == null && request.getCode() != null && !request.getCode().isBlank()) {
            String codeUpper = request.getCode().trim().toUpperCase();
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("MR-\\d+").matcher(codeUpper);
            if (matcher.find()) {
                code = matcher.group();
            } else if (codeUpper.startsWith("MR-")) {
                code = codeUpper;
            } else if (codeUpper.matches("\\d+")) {
                code = "MR-" + codeUpper;
            }
        }

        if (code == null || !code.startsWith("MR-")) {
            throw new BusinessException("Medical Record code was not found in SePay webhook payload");
        }

        Long medicalRecordId = Long.parseLong(code.substring(3));

        MedicalRecord medicalRecord = medicalRecordRepository.findByIdForUpdate(medicalRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with id: " + medicalRecordId));

        PaymentRecord paymentRecord = paymentRecordRepository.findByMedicalRecordIdForUpdate(medicalRecordId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Medical record payment record not found for id: " + medicalRecordId
                ));

        entityManager.refresh(medicalRecord);
        entityManager.refresh(paymentRecord);

        if (paymentRecord.getPaymentStatus() == PaymentStatus.PAID) {
            return toResponseWithTransactions(paymentRecord);
        }

        if (medicalRecord.getStatus() != MedicalRecordStatus.DRAFT) {
            throw new BusinessException("Only draft medical records can be paid and moved to IN_PROGRESS");
        }

        BigDecimal transferAmount = request.getTransferAmount() == null ? BigDecimal.ZERO : BigDecimal.valueOf(request.getTransferAmount());
        if (paymentRecord.getTotalPrice().compareTo(transferAmount) != 0) {
            throw new BusinessException("Transfer amount does not match expected medical record total price: " + paymentRecord.getTotalPrice());
        }

        paymentRecord.setReceivedAmount(transferAmount);
        paymentRecord.setPaidAt(LocalDateTime.now());
        paymentRecord.setPaymentStatus(PaymentStatus.PAID);

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setPaymentRecord(paymentRecord);
        transaction.setSepayTransactionId(request.getId() != null ? request.getId().toString() : null);
        transaction.setGateway(request.getGateway() != null && !request.getGateway().isBlank() ? request.getGateway() : "SEPAY");
        transaction.setTransferType(request.getTransferType());
        transaction.setTransferAmount(transferAmount);
        transaction.setTransactionDate(paymentRecord.getPaidAt());
        transaction.setReferenceCode(request.getReferenceCode());
        transaction.setAccountNumber(request.getAccountNumber());
        transaction.setContent(request.getContent());
        transaction.setDescription(request.getDescription());
        transaction.setRawData(request.getContent());
        transaction.setProcessStatus(PaymentTransactionStatus.SUCCESS);

        medicalRecord.setStatus(MedicalRecordStatus.IN_PROGRESS);

        try {
            paymentTransactionRepository.save(transaction);
            paymentRecordRepository.save(paymentRecord);
            medicalRecordRepository.saveAndFlush(medicalRecord);
            entityManager.refresh(medicalRecord);

            propagateInternalRequestPayments(medicalRecordId);

            return toResponseWithTransactions(paymentRecord);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException("Payment confirmation conflict detected");
        }
    }

    private void propagateInternalRequestPayments(Long medicalRecordId) {
        List<LabTestRequest> labTests = labTestRequestRepository.findByMedRecord_MedicalRecordId(medicalRecordId);
        for (LabTestRequest ltr : labTests) {
            ltr.setPaymentStatus(PaymentStatus.PAID);
            ltr.setPaidAt(LocalDateTime.now());
            labTestRequestRepository.save(ltr);
        }

        List<MedicalServiceRequest> serviceRequests = medicalServiceRequestRepository.findByMedRecord_MedicalRecordId(medicalRecordId);
        for (MedicalServiceRequest msr : serviceRequests) {
            msr.setPaymentStatus(PaymentStatus.PAID);
            msr.setPaidAt(LocalDateTime.now());
            medicalServiceRequestRepository.save(msr);
        }
    }

    private void validateSepaySecret(String secretKeyHeader) {
        if (sepayWebhookSecret == null || sepayWebhookSecret.isBlank()) {
            throw new BusinessException("SePay webhook secret is not configured");
        }

        if (!sepayWebhookSecret.equals(secretKeyHeader)) {
            throw new BusinessException("Invalid SePay webhook secret");
        }
    }

    @Override
    @Transactional
    public PaymentRecordResponse recordPrescriptionCashPayment(Long prescriptionId) {
        Account currentAccount = findCurrentAccountOrThrow();
        PaymentRecord paymentRecord = paymentRecordRepository.findByPrescriptionIdForUpdate(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Prescription payment record not found for id: " + prescriptionId
                ));

        if (paymentRecord.getPaymentStatus() == PaymentStatus.PAID) {
            return toResponseWithTransactions(paymentRecord);
        }

        paymentRecord.setReceivedAmount(paymentRecord.getTotalPrice());
        paymentRecord.setPaidAt(LocalDateTime.now());
        paymentRecord.setPaymentStatus(PaymentStatus.PAID);

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setPaymentRecord(paymentRecord);
        transaction.setGateway("CASH");
        transaction.setTransferType("cash");
        transaction.setTransferAmount(paymentRecord.getTotalPrice());
        transaction.setTransactionDate(paymentRecord.getPaidAt());
        transaction.setContent("Thanh toán tiền mặt đơn thuốc " + paymentRecord.getRequestCode());
        transaction.setDescription("Thanh toán tiền mặt đơn thuốc " + paymentRecord.getRequestCode());
        transaction.setRawData("CASH PAYMENT");
        transaction.setConfirmedBy(currentAccount);
        transaction.setProcessStatus(PaymentTransactionStatus.SUCCESS);

        paymentTransactionRepository.save(transaction);
        paymentRecordRepository.save(paymentRecord);

        return toResponseWithTransactions(paymentRecord);
    }

    @Override
    @Transactional
    public PaymentRecordResponse confirmPrescriptionPaymentFromSepayWebhook(
            com.healthcare.backend.dto.request.SepayWebhookRequest request,
            String secretKeyHeader
    ) {
        validateSepaySecret(secretKeyHeader);
        validateIncomingTransfer(request);

        if (request.getId() != null) {
            PaymentTransaction existingTransaction = paymentTransactionRepository
                    .findBySepayTransactionId(request.getId().toString())
                    .orElse(null);
            if (existingTransaction != null) {
                return toResponseWithTransactions(existingTransaction.getPaymentRecord());
            }
        }

        String code = null;
        if (request.getContent() != null && !request.getContent().isBlank()) {
            String contentUpper = request.getContent().toUpperCase();
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("PR-\\d+").matcher(contentUpper);
            if (matcher.find()) {
                code = matcher.group();
            }
        }
        if (code == null && request.getDescription() != null && !request.getDescription().isBlank()) {
            String descUpper = request.getDescription().toUpperCase();
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("PR-\\d+").matcher(descUpper);
            if (matcher.find()) {
                code = matcher.group();
            }
        }
        if (code == null && request.getCode() != null && !request.getCode().isBlank()) {
            String codeUpper = request.getCode().trim().toUpperCase();
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("PR-\\d+").matcher(codeUpper);
            if (matcher.find()) {
                code = matcher.group();
            } else if (codeUpper.startsWith("PR-")) {
                code = codeUpper;
            } else if (codeUpper.matches("\\d+")) {
                code = "PR-" + codeUpper;
            }
        }

        if (code == null || !code.startsWith("PR-")) {
            throw new BusinessException("Prescription code was not found in SePay webhook payload");
        }

        Long prescriptionId = Long.parseLong(code.substring(3));

        PaymentRecord paymentRecord = paymentRecordRepository.findByPrescriptionIdForUpdate(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Prescription payment record not found for id: " + prescriptionId
                ));

        if (paymentRecord.getPaymentStatus() == PaymentStatus.PAID) {
            return toResponseWithTransactions(paymentRecord);
        }

        BigDecimal transferAmount = request.getTransferAmount() == null ? BigDecimal.ZERO : BigDecimal.valueOf(request.getTransferAmount());
        if (paymentRecord.getTotalPrice().compareTo(transferAmount) != 0) {
            throw new BusinessException("Transfer amount does not match expected prescription total price: " + paymentRecord.getTotalPrice());
        }

        paymentRecord.setReceivedAmount(transferAmount);
        paymentRecord.setPaidAt(LocalDateTime.now());
        paymentRecord.setPaymentStatus(PaymentStatus.PAID);

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setPaymentRecord(paymentRecord);
        transaction.setSepayTransactionId(request.getId() != null ? request.getId().toString() : null);
        transaction.setGateway(request.getGateway() != null && !request.getGateway().isBlank() ? request.getGateway() : "SEPAY");
        transaction.setTransferType(request.getTransferType());
        transaction.setTransferAmount(transferAmount);
        transaction.setTransactionDate(paymentRecord.getPaidAt());
        transaction.setReferenceCode(request.getReferenceCode());
        transaction.setAccountNumber(request.getAccountNumber());
        transaction.setContent(request.getContent());
        transaction.setDescription(request.getDescription());
        transaction.setRawData(request.getContent());
        transaction.setProcessStatus(PaymentTransactionStatus.SUCCESS);

        try {
            paymentTransactionRepository.save(transaction);
            paymentRecordRepository.save(paymentRecord);
            return toResponseWithTransactions(paymentRecord);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException("Payment confirmation conflict detected");
        }
    }

    private void validateIncomingTransfer(com.healthcare.backend.dto.request.SepayWebhookRequest request) {
        if (request == null) {
            throw new BusinessException("SePay webhook payload must not be null");
        }

        if (!"in".equalsIgnoreCase(request.getTransferType())) {
            throw new BusinessException("Only incoming transfers can confirm payments");
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
}
