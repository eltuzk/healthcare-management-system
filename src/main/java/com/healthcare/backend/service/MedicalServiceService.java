package com.healthcare.backend.service;

import com.healthcare.backend.dto.request.MedicalServiceRequest;
import com.healthcare.backend.dto.response.MedicalServiceResponse;

import java.util.List;

public interface MedicalServiceService {

    List<MedicalServiceResponse> getAllMedicalServices();

    MedicalServiceResponse getMedicalServiceById(Long id);

    MedicalServiceResponse createMedicalService(MedicalServiceRequest request);

    MedicalServiceResponse updateMedicalService(Long id, MedicalServiceRequest request);

    MedicalServiceResponse deactivateMedicalService(Long id);
}