package com.healthcare.backend.service.impl;

import com.healthcare.backend.dto.request.AdmissionRecordRequest;
import com.healthcare.backend.dto.request.AdmissionRequestRequest;
import com.healthcare.backend.dto.request.AdmissionStatusUpdateRequest;
import com.healthcare.backend.dto.response.AdmissionRecordResponse;
import com.healthcare.backend.dto.response.AdmissionRequestResponse;
import com.healthcare.backend.entity.AdmissionRecord;
import com.healthcare.backend.entity.AdmissionRequest;
import com.healthcare.backend.entity.Bed;
import com.healthcare.backend.entity.MedicalRecord;
import com.healthcare.backend.entity.Patient;
import com.healthcare.backend.entity.enums.AdmissionStatus;
import com.healthcare.backend.exception.DuplicateResourceException;
import com.healthcare.backend.exception.BusinessException;
import com.healthcare.backend.exception.ResourceNotFoundException;
import com.healthcare.backend.mapper.AdmissionRecordMapper;
import com.healthcare.backend.mapper.AdmissionRequestMapper;
import com.healthcare.backend.repository.AdmissionRecordRepository;
import com.healthcare.backend.repository.AdmissionRequestRepository;
import com.healthcare.backend.repository.BedRepository;
import com.healthcare.backend.repository.MedicalRecordRepository;
import com.healthcare.backend.repository.PatientRepository;
import com.healthcare.backend.service.AdmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdmissionServiceImpl implements AdmissionService {

    private final AdmissionRequestRepository admissionRequestRepository;
    private final AdmissionRecordRepository  admissionRecordRepository;
    private final PatientRepository          patientRepository;
    private final MedicalRecordRepository    medicalRecordRepository;
    private final BedRepository              bedRepository;
    private final AdmissionRequestMapper     admissionRequestMapper;
    private final AdmissionRecordMapper      admissionRecordMapper;

    // ─────────────────────────────────────────────────────────────
    // AdmissionRequest
    // ─────────────────────────────────────────────────────────────

    @Override
    public List<AdmissionRequestResponse> getAll() {
        return admissionRequestRepository.findAll()
                .stream()
                .map(admissionRequestMapper::toResponse)
                .toList();
    }

    @Override
    public AdmissionRequestResponse getById(Long admissionId) {
        return admissionRequestMapper.toResponse(findAdmissionOrThrow(admissionId));
    }

    @Override
    public List<AdmissionRequestResponse> getByPatientId(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with id: " + patientId);
        }
        return admissionRequestRepository.findAllByPatient_PatientId(patientId)
                .stream()
                .map(admissionRequestMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AdmissionRequestResponse create(AdmissionRequestRequest request) {

        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found with id: " + request.getPatientId()));

        MedicalRecord medicalRecord = medicalRecordRepository.findById(request.getMedRecordId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Medical record not found with id: " + request.getMedRecordId()));

        if (admissionRequestRepository.existsByMedicalRecord_MedicalRecordId(request.getMedRecordId())) {
            throw new DuplicateResourceException("This medical record already has an admission request");
        }

        Bed bed = bedRepository.findById(request.getBedId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bed not found with id: " + request.getBedId()));

        if (bed.getStatus() != Bed.BedStatus.AVAILABLE) {
            throw new BusinessException(
                    "Bed is not available, current status: " + bed.getStatus());
        }

        if (request.getDischargeDate() != null
                && !request.getDischargeDate().isAfter(request.getAdmissionDate())) {
            throw new BusinessException("Discharge date must be after admission date");
        }


        AdmissionRequest admission = admissionRequestMapper.toEntity(request);
        admission.setPatient(patient);
        admission.setMedicalRecord(medicalRecord);
        admission.setBed(bed);

        return admissionRequestMapper.toResponse(admissionRequestRepository.save(admission));
    }

    @Override
    @Transactional
    public AdmissionRequestResponse updateStatus(Long admissionId, AdmissionStatusUpdateRequest request) {

        AdmissionRequest admission = admissionRequestRepository.findById(admissionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Admission request not found with id: " + admissionId));

        validateStatusTransition(admission.getStatus(), request.getStatus());

        switch (request.getStatus()) {
            case ADMITTED   -> handleAdmit(admission);
            case DISCHARGED -> handleDischarge(admission, request);
            case CANCELLED  -> handleCancel(admission);
            default         -> throw new BusinessException(
                    "Invalid status: " + request.getStatus());
        }

        admission.setStatus(request.getStatus());
        return admissionRequestMapper.toResponse(admissionRequestRepository.save(admission));
    }

    // ─────────────────────────────────────────────────────────────
    // AdmissionRecord
    // ─────────────────────────────────────────────────────────────

    @Override
    public List<AdmissionRecordResponse> getRecords(Long admissionId) {
        findAdmissionOrThrow(admissionId);
        return admissionRecordRepository.findAllByAdmissionRequest_AdmissionId(admissionId)
                .stream()
                .map(admissionRecordMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AdmissionRecordResponse createRecord(Long admissionId, AdmissionRecordRequest request) {

        AdmissionRequest admission = findAdmissionOrThrow(admissionId);

        if (admission.getStatus() != AdmissionStatus.ADMITTED) {
            throw new BusinessException(
                    "Vital signs can only be recorded when the patient is admitted. " +
                            "Current status: " + admission.getStatus());
        }

        AdmissionRecord record = admissionRecordMapper.toEntity(request);
        record.setAdmissionRequest(admission);

        return admissionRecordMapper.toResponse(admissionRecordRepository.save(record));
    }

    @Override
    @Transactional
    public AdmissionRecordResponse updateRecord(Long recordId, AdmissionRecordRequest request) {

        AdmissionRecord record = admissionRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Admission record not found with id: " + recordId));

        if (record.getAdmissionRequest().getStatus() == AdmissionStatus.DISCHARGED) {
            throw new BusinessException(
                    "Cannot edit records of a closed admission");
        }

        admissionRecordMapper.updateEntityFromRequest(request, record);

        return admissionRecordMapper.toResponse(admissionRecordRepository.save(record));
    }

    // ─────────────────────────────────────────────────────────────
    // Helper Func
    // ─────────────────────────────────────────────────────────────

    private void validateStatusTransition(AdmissionStatus current, AdmissionStatus next) {
        boolean valid = switch (current) {
            case PENDING  -> next == AdmissionStatus.ADMITTED || next == AdmissionStatus.CANCELLED;
            case ADMITTED -> next == AdmissionStatus.DISCHARGED;
            default       -> false;
        };
        if (!valid) {
            throw new BusinessException(
                    "Cannot transition status from " + current + " to " + next);
        }
    }

    private void handleAdmit(AdmissionRequest admission) {
        Bed bed = admission.getBed();
        if (bed.getStatus() != Bed.BedStatus.AVAILABLE) {
            throw new BusinessException(
                    "Bed is no longer available, status: " + bed.getStatus());
        }
        bed.setStatus(Bed.BedStatus.OCCUPIED);
        bedRepository.save(bed);
    }

    private void handleDischarge(AdmissionRequest admission, AdmissionStatusUpdateRequest request) {
        if (request.getDischargeDate() == null) {
            throw new BusinessException("Discharge date is required upon discharge");
        }
        if (!request.getDischargeDate().isAfter(admission.getAdmissionDate())) {
            throw new BusinessException("Discharge date must be after admission date");
        }

        long days = ChronoUnit.DAYS.between(
                admission.getAdmissionDate(), request.getDischargeDate());

        admission.setDischargeDate(request.getDischargeDate());
        admission.setTotalPrice(
                admission.getBed().getPrice().multiply(BigDecimal.valueOf(days)));

        Bed bed = admission.getBed();
        bed.setStatus(Bed.BedStatus.AVAILABLE);
        bedRepository.save(bed);
    }

    private void handleCancel(AdmissionRequest admission) {
        Bed bed = admission.getBed();
        if (bed.getStatus() == Bed.BedStatus.OCCUPIED) {
            bed.setStatus(Bed.BedStatus.AVAILABLE);
            bedRepository.save(bed);
        }
    }

    private AdmissionRequest findAdmissionOrThrow(Long id) {
        return admissionRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Admission request not found with id: " + id));
    }
}