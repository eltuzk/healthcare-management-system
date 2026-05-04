package com.healthcare.backend.service;

import com.healthcare.backend.dto.request.CreateMedicalRecordRequest;
import com.healthcare.backend.dto.request.UpdateMedicalRecordRequest;
import com.healthcare.backend.dto.response.MedicalRecordResponse;
import com.healthcare.backend.entity.enums.MedicalRecordStatus;

import java.time.LocalDate;
import java.util.List;

public interface MedicalRecordService {

    MedicalRecordResponse createFromAppointment(Long appointmentId, CreateMedicalRecordRequest request);

    MedicalRecordResponse getById(Long medicalRecordId);

    List<MedicalRecordResponse> getAll(Long patientId, Long doctorId, MedicalRecordStatus status, LocalDate date);

    MedicalRecordResponse update(Long medicalRecordId, UpdateMedicalRecordRequest request);

    MedicalRecordResponse complete(Long medicalRecordId);

    MedicalRecordResponse lock(Long medicalRecordId);

    void validateEligibleForAdmission(Long medicalRecordId);
}
