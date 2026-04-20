package com.healthcare.backend.mapper;

import com.healthcare.backend.dto.request.PatientInsuranceRequest;
import com.healthcare.backend.dto.response.PatientInsuranceResponse;
import com.healthcare.backend.entity.PatientInsurance;
import org.springframework.stereotype.Component;

@Component
public class PatientInsuranceMapper {

    public PatientInsurance toEntity(PatientInsuranceRequest request) {
        PatientInsurance entity = new PatientInsurance();
        entity.setInsuranceNum(request.getInsuranceNum());
        entity.setCoveragePercent(request.getCoveragePercent());
        entity.setExpiryDate(request.getExpiryDate());
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        // patient is set by service
        return entity;
    }

    public PatientInsuranceResponse toResponse(PatientInsurance entity) {
        PatientInsuranceResponse response = new PatientInsuranceResponse();
        response.setPatientInsuranceId(entity.getPatientInsuranceId());
        response.setInsuranceNum(entity.getInsuranceNum());
        response.setStatus(entity.getStatus());
        response.setExpiryDate(entity.getExpiryDate());
        response.setCoveragePercent(entity.getCoveragePercent());
        if (entity.getPatient() != null) {
            response.setPatientId(entity.getPatient().getPatientId());
            response.setFullName(entity.getPatient().getFullName());
        }
        return response;
    }

    public void updateEntityFromRequest(PatientInsuranceRequest request, PatientInsurance entity) {
        if (request.getInsuranceNum() != null) entity.setInsuranceNum(request.getInsuranceNum());
        if (request.getCoveragePercent() != null) entity.setCoveragePercent(request.getCoveragePercent());
        if (request.getExpiryDate() != null) entity.setExpiryDate(request.getExpiryDate());
        if (request.getStatus() != null) entity.setStatus(request.getStatus());
        // patient is NOT updated
    }
}
