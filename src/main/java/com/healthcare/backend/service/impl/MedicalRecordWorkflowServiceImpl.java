package com.healthcare.backend.service.impl;

import com.healthcare.backend.entity.MedicalRecord;
import com.healthcare.backend.entity.PaymentRecord;
import com.healthcare.backend.entity.enums.AppointmentStatus;
import com.healthcare.backend.entity.enums.LabTestRequestStatus;
import com.healthcare.backend.entity.enums.MedicalRecordConclusionType;
import com.healthcare.backend.entity.enums.MedicalRecordStatus;
import com.healthcare.backend.entity.enums.MedicalServiceRequestStatus;
import com.healthcare.backend.entity.enums.PaymentStatus;
import com.healthcare.backend.exception.BusinessException;
import com.healthcare.backend.exception.ResourceNotFoundException;
import com.healthcare.backend.repository.LabTestRequestRepository;
import com.healthcare.backend.repository.MedicalRecordRepository;
import com.healthcare.backend.repository.MedicalServiceRequestRepository;
import com.healthcare.backend.repository.PaymentRecordRepository;
import com.healthcare.backend.service.MedicalRecordWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MedicalRecordWorkflowServiceImpl implements MedicalRecordWorkflowService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final LabTestRequestRepository labTestRequestRepository;
    private final MedicalServiceRequestRepository medicalServiceRequestRepository;
    private final PaymentRecordRepository paymentRecordRepository;

    @Override
    public void validateCanCreateRequest(MedicalRecord medicalRecord) {
        MedicalRecord lockedMedicalRecord = lockMedicalRecord(medicalRecord);
        if (lockedMedicalRecord.getStatus() != MedicalRecordStatus.DRAFT) {
            throw new BusinessException("Requests can only be created while medical record is DRAFT");
        }
    }

    @Override
    public void validateCanUpdateRequest(MedicalRecord medicalRecord) {
        MedicalRecord lockedMedicalRecord = lockMedicalRecord(medicalRecord);
        if (lockedMedicalRecord.getStatus() != MedicalRecordStatus.IN_PROGRESS) {
            throw new BusinessException("Requests can only be updated after medical record payment is recorded");
        }

        PaymentRecord paymentRecord = paymentRecordRepository
                .findByMedicalRecord_MedicalRecordId(lockedMedicalRecord.getMedicalRecordId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Medical record payment record not found for id: " + lockedMedicalRecord.getMedicalRecordId()
                ));

        if (paymentRecord.getPaymentStatus() != PaymentStatus.PAID) {
            throw new BusinessException("Medical record must be fully paid before updating requests");
        }
    }

    @Override
    public void validateReadyToComplete(MedicalRecord medicalRecord) {
        if (medicalRecord.getStatus() != MedicalRecordStatus.IN_PROGRESS
                && medicalRecord.getStatus() != MedicalRecordStatus.DRAFT) {
            throw new BusinessException("Only in-progress or draft medical records can be completed");
        }

        if (!allRequestsHaveResults(medicalRecord.getMedicalRecordId())) {
            throw new BusinessException("All lab test and medical service requests must have results before completion");
        }
    }

    @Override
    @Transactional
    public void completeIfReady(Long medicalRecordId) {
        MedicalRecord medicalRecord = medicalRecordRepository.findByIdForUpdate(medicalRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with id: " + medicalRecordId));

        if ((medicalRecord.getStatus() != MedicalRecordStatus.IN_PROGRESS && medicalRecord.getStatus() != MedicalRecordStatus.DRAFT)
                || !allRequestsHaveResults(medicalRecordId)) {
            return;
        }

        medicalRecord.setStatus(MedicalRecordStatus.COMPLETED);
        medicalRecord.setCompletedAt(LocalDateTime.now());
        medicalRecord.getAppointment().setStatus(AppointmentStatus.COMPLETED);
        medicalRecordRepository.save(medicalRecord);
    }

    private boolean allRequestsHaveResults(Long medicalRecordId) {
        return labTestRequestRepository.countByMedRecord_MedicalRecordIdAndStatusNot(
                medicalRecordId,
                LabTestRequestStatus.RESULT_AVAILABLE
        ) == 0
                && medicalServiceRequestRepository.countByMedRecord_MedicalRecordIdAndStatusNot(
                medicalRecordId,
                MedicalServiceRequestStatus.RESULT_AVAILABLE
        ) == 0;
    }

    private MedicalRecord lockMedicalRecord(MedicalRecord medicalRecord) {
        if (medicalRecord == null || medicalRecord.getMedicalRecordId() == null) {
            throw new ResourceNotFoundException("Medical record not found");
        }

        return medicalRecordRepository.findByIdForUpdate(medicalRecord.getMedicalRecordId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Medical record not found with id: " + medicalRecord.getMedicalRecordId()
                ));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
