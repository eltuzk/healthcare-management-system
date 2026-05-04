package com.healthcare.backend.mapper;

import com.healthcare.backend.dto.request.MedicalServiceRequest;
import com.healthcare.backend.dto.response.MedicalServiceResponse;
import com.healthcare.backend.entity.MedicalService;
import org.springframework.stereotype.Component;

@Component
public class MedicalServiceMapper {

    public MedicalService toEntity(MedicalServiceRequest request) {
        if (request == null) {
            return null;
        }

        MedicalService medicalService = new MedicalService();
        medicalService.setMedicalServiceName(normalize(request.getMedicalServiceName()));
        medicalService.setPrice(request.getPrice());
        medicalService.setIsActive(1);

        return medicalService;
    }

    public void updateEntityFromRequest(
            MedicalServiceRequest request,
            MedicalService medicalService
    ) {
        if (request == null || medicalService == null) {
            return;
        }

        medicalService.setMedicalServiceName(normalize(request.getMedicalServiceName()));
        medicalService.setPrice(request.getPrice());
    }

    public MedicalServiceResponse toResponse(MedicalService medicalService) {
        if (medicalService == null) {
            return null;
        }

        MedicalServiceResponse response = new MedicalServiceResponse();
        response.setMedServiceId(medicalService.getMedServiceId());
        response.setMedicalServiceName(medicalService.getMedicalServiceName());
        response.setPrice(medicalService.getPrice());
        response.setActive(Integer.valueOf(1).equals(medicalService.getIsActive()));

        return response;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}