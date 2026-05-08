package com.healthcare.backend.service;

import com.healthcare.backend.entity.MedicalRecord;

public interface MedicalRecordWorkflowService {

    void validateCanCreateRequest(MedicalRecord medicalRecord);

    void validateCanUpdateRequest(MedicalRecord medicalRecord);

    void validateReadyToComplete(MedicalRecord medicalRecord);

    void completeIfReady(Long medicalRecordId);
}
