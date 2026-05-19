package com.healthcare.backend.service.impl;

import com.healthcare.backend.dto.request.CreateMedicalRecordRequest;
import com.healthcare.backend.dto.request.UpdateMedicalRecordRequest;
import com.healthcare.backend.dto.response.MedicalRecordResponse;
import com.healthcare.backend.entity.Account;
import com.healthcare.backend.entity.Appointment;
import com.healthcare.backend.entity.Doctor;
import com.healthcare.backend.entity.MedicalRecord;
import com.healthcare.backend.entity.enums.AppointmentStatus;
import com.healthcare.backend.entity.enums.MedicalRecordConclusionType;
import com.healthcare.backend.entity.enums.MedicalRecordStatus;
import com.healthcare.backend.exception.BusinessException;
import com.healthcare.backend.exception.DuplicateResourceException;
import com.healthcare.backend.exception.ResourceNotFoundException;
import com.healthcare.backend.mapper.MedicalRecordMapper;
import com.healthcare.backend.repository.AccountRepository;
import com.healthcare.backend.repository.AppointmentRepository;
import com.healthcare.backend.repository.DoctorRepository;
import com.healthcare.backend.repository.MedicalRecordRepository;
import com.healthcare.backend.repository.PatientRepository;
import com.healthcare.backend.service.MedicalRecordBillingService;
import com.healthcare.backend.service.MedicalRecordService;
import com.healthcare.backend.service.MedicalRecordWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final AccountRepository accountRepository;
    private final PatientRepository patientRepository;
    private final MedicalRecordMapper medicalRecordMapper;
    private final MedicalRecordBillingService medicalRecordBillingService;
    private final MedicalRecordWorkflowService medicalRecordWorkflowService;

    @Override
    // Transaction đảm bảo kiểm tra appointment, chống tạo trùng MR và lưu hồ sơ ban đầu cùng một đơn vị atomic.
    @Transactional
    public MedicalRecordResponse createFromAppointment(Long appointmentId, CreateMedicalRecordRequest request) {
        Appointment appointment = findAppointmentOrThrow(appointmentId);

        if (appointment.getStatus() != AppointmentStatus.IN_PROGRESS) {
            throw new BusinessException("Medical record can only be created for appointments in progress");
        }

        if (medicalRecordRepository.existsByAppointment_AppointmentId(appointmentId)) {
            throw new DuplicateResourceException("Medical record already exists for appointment id: " + appointmentId);
        }

        Doctor currentDoctor = resolveDoctorForAppointment(appointment);

        MedicalRecord medicalRecord = medicalRecordMapper.toEntity(request);
        medicalRecord.setAppointment(appointment);
        medicalRecord.setDoctor(currentDoctor);
        medicalRecord.setPatient(appointment.getPatient());
        medicalRecord.setStatus(MedicalRecordStatus.DRAFT);
        medicalRecord.setTotalPrice(BigDecimal.ZERO);

        try {
            MedicalRecord savedMedicalRecord = medicalRecordRepository.saveAndFlush(medicalRecord);
            medicalRecordBillingService.initializePaymentRecord(savedMedicalRecord);
            return medicalRecordMapper.toResponse(savedMedicalRecord);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException("Medical record already exists for appointment id: " + appointmentId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalRecordResponse getById(Long medicalRecordId) {
        return medicalRecordMapper.toResponse(findMedicalRecordOrThrow(medicalRecordId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> getAll(Long patientId, Long doctorId, MedicalRecordStatus status, LocalDate date) {
        if (isCurrentUserPatient()) {
            Long currentPatientId = findCurrentPatientOrThrow().getPatientId();
            if (patientId != null && !patientId.equals(currentPatientId)) {
                throw new BusinessException("Patient is not allowed to access other patients' medical records");
            }
            patientId = currentPatientId;
        }

        LocalDateTime fromDate = date != null ? date.atStartOfDay() : null;
        LocalDateTime toDate = date != null ? date.plusDays(1).atStartOfDay() : null;

        return medicalRecordRepository.findAllByFilters(patientId, doctorId, status, fromDate, toDate)
                .stream()
                .sorted(Comparator.comparing(MedicalRecord::getCreatedAt).reversed())
                .map(medicalRecordMapper::toResponse)
                .toList();
    }

    @Override
    // Transaction dùng để validate version và cập nhật nội dung MR trong cùng một persistence context.
    @Transactional
    public MedicalRecordResponse update(Long medicalRecordId, UpdateMedicalRecordRequest request) {
        MedicalRecord medicalRecord = findMedicalRecordOrThrow(medicalRecordId);

        validateMedicalRecordAccess(medicalRecord);
        validateEditableState(medicalRecord);
        validateVersion(request.getVersion(), medicalRecord);

        medicalRecordMapper.updateEntityFromRequest(request, medicalRecord);
        MedicalRecord savedMedicalRecord = medicalRecordRepository.save(medicalRecord);
        medicalRecordWorkflowService.completeIfReady(savedMedicalRecord.getMedicalRecordId());
        return medicalRecordMapper.toResponse(savedMedicalRecord);
    }

    @Override
    // Transaction đảm bảo complete MR và complete appointment đi cùng nhau, không bị lệch vòng đời.
    @Transactional
    public MedicalRecordResponse complete(Long medicalRecordId) {
        MedicalRecord medicalRecord = findMedicalRecordForUpdateOrThrow(medicalRecordId);

        validateMedicalRecordAccess(medicalRecord);
        validateEditableState(medicalRecord);
        medicalRecordWorkflowService.validateReadyToComplete(medicalRecord);

        medicalRecord.setStatus(MedicalRecordStatus.COMPLETED);
        medicalRecord.setCompletedAt(LocalDateTime.now());
        medicalRecord.getAppointment().setStatus(AppointmentStatus.COMPLETED);

        return medicalRecordMapper.toResponse(medicalRecordRepository.save(medicalRecord));
    }

    @Override
    // Transaction dùng để khóa hồ sơ sau khi hoàn tất, đảm bảo trạng thái LOCKED được lưu nhất quán.
    @Transactional
    public MedicalRecordResponse lock(Long medicalRecordId) {
        MedicalRecord medicalRecord = findMedicalRecordForUpdateOrThrow(medicalRecordId);

        validateMedicalRecordAccess(medicalRecord);
        if (medicalRecord.getStatus() != MedicalRecordStatus.COMPLETED) {
            throw new BusinessException("Only completed medical records can be locked");
        }

        medicalRecord.setStatus(MedicalRecordStatus.LOCKED);
        medicalRecord.setLockedAt(LocalDateTime.now());

        return medicalRecordMapper.toResponse(medicalRecordRepository.save(medicalRecord));
    }

    @Override
    @Transactional(readOnly = true)
    public void validateEligibleForAdmission(Long medicalRecordId) {
        MedicalRecord medicalRecord = findMedicalRecordOrThrow(medicalRecordId);
        if (medicalRecord.getConclusionType() != MedicalRecordConclusionType.ADMISSION_REQUIRED) {
            throw new BusinessException("Medical record is not eligible for admission request");
        }
    }

    private MedicalRecord findMedicalRecordOrThrow(Long medicalRecordId) {
        return medicalRecordRepository.findById(medicalRecordId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Medical record not found with id: " + medicalRecordId
                ));
    }

    private MedicalRecord findMedicalRecordForUpdateOrThrow(Long medicalRecordId) {
        return medicalRecordRepository.findByIdForUpdate(medicalRecordId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Medical record not found with id: " + medicalRecordId
                ));
    }

    private Appointment findAppointmentOrThrow(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with id: " + appointmentId
                ));
    }

    private Doctor findCurrentDoctorOrThrow() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with email: " + email));

        Doctor doctor = doctorRepository.findByAccount_AccountId(account.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for account: " + email));

        if (!doctor.isActive()) {
            throw new ResourceNotFoundException("Doctor profile not found for account: " + email);
        }

        return doctor;
    }

    private Doctor resolveDoctorForAppointment(Appointment appointment) {
        if (isCurrentUserAdmin()) {
            return appointment.getDoctorSchedule().getDoctor();
        }

        Doctor currentDoctor = findCurrentDoctorOrThrow();
        validateDoctorOwnership(currentDoctor, appointment);
        return currentDoctor;
    }

    private void validateMedicalRecordAccess(MedicalRecord medicalRecord) {
        if (isCurrentUserAdmin() || isCurrentUserReceptionist()) {
            return;
        }

        if (isCurrentUserPatient()) {
            com.healthcare.backend.entity.Patient currentPatient = findCurrentPatientOrThrow();
            if (!currentPatient.getPatientId().equals(medicalRecord.getPatient().getPatientId())) {
                throw new BusinessException("Patient is not allowed to access this medical record");
            }
            return;
        }

        validateDoctorOwnership(findCurrentDoctorOrThrow(), medicalRecord);
    }

    private boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.getAuthorities().stream()
                        .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private boolean isCurrentUserReceptionist() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.getAuthorities().stream()
                        .anyMatch(authority -> "ROLE_RECEPTIONIST".equals(authority.getAuthority()));
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

    private void validateDoctorOwnership(Doctor doctor, Appointment appointment) {
        Long ownerDoctorId = appointment.getDoctorSchedule().getDoctor().getDoctorId();
        if (!doctor.getDoctorId().equals(ownerDoctorId)) {
            throw new BusinessException("Doctor is not allowed to access this appointment");
        }
    }

    private void validateDoctorOwnership(Doctor doctor, MedicalRecord medicalRecord) {
        if (!doctor.getDoctorId().equals(medicalRecord.getDoctor().getDoctorId())) {
            throw new BusinessException("Doctor is not allowed to access this medical record");
        }
    }

    private void validateEditableState(MedicalRecord medicalRecord) {
        if (medicalRecord.getStatus() != MedicalRecordStatus.DRAFT
                && medicalRecord.getStatus() != MedicalRecordStatus.IN_PROGRESS
                && medicalRecord.getStatus() != MedicalRecordStatus.COMPLETED) {
            throw new BusinessException("Medical record is not editable");
        }
    }

    private void validateVersion(Long requestVersion, MedicalRecord medicalRecord) {
        // Optimistic locking thủ công: client phải gửi version đang thấy.
        // Nếu version lệch nghĩa là có request khác đã sửa MR trước đó, tránh ghi đè dữ liệu khám.
        if (!medicalRecord.getVersionNumber().equals(requestVersion)) {
            throw new ObjectOptimisticLockingFailureException(MedicalRecord.class, medicalRecord.getMedicalRecordId());
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
