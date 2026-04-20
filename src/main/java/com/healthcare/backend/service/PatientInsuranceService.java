package com.healthcare.backend.service;

import com.healthcare.backend.dto.request.PatientInsuranceRequest;
import com.healthcare.backend.dto.response.PatientInsuranceResponse;

import java.util.List;

public interface PatientInsuranceService {

    List<PatientInsuranceResponse> getByPatientId(Long patientId);

    PatientInsuranceResponse create(PatientInsuranceRequest request);

    PatientInsuranceResponse update(Long id, PatientInsuranceRequest request);

    void delete(Long id);
}
