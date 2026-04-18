package com.healthcare.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.healthcare.backend.dto.request.PatientRequest;
import com.healthcare.backend.dto.response.PatientResponse;

public interface PatientService {
    Page<PatientResponse> getAllPatients(Pageable pageable);

    PatientResponse getPatientById(Long patientId);

    PatientResponse createPatient(PatientRequest patientRequest);

    PatientResponse updatePatient(Long patientId, PatientRequest patientRequest);

    void deletePatient(Long patientId);
}
