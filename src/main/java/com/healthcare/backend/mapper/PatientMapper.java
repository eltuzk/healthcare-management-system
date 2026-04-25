package com.healthcare.backend.mapper;

import com.healthcare.backend.dto.request.PatientRequest;
import com.healthcare.backend.dto.response.PatientResponse;
import com.healthcare.backend.entity.Patient;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class PatientMapper {

    public Patient toEntity(PatientRequest request) {
        Patient entity = new Patient();
        entity.setFullName(request.getFullName());
        entity.setGender(normalizeGender(request.getGender()));
        entity.setDateOfBirth(request.getDateOfBirth());
        entity.setPhone(request.getPhone());
        entity.setAddress(request.getAddress());
        entity.setIdentityNum(request.getIdentityNum());
        entity.setMedicalHistory(request.getMedicalHistory());
        entity.setAllergy(request.getAllergy());
        // account and isActive are set by service
        return entity;
    }

    public PatientResponse toResponse(Patient entity) {
        PatientResponse response = new PatientResponse();
        response.setPatientId(entity.getPatientId());
        response.setFullName(entity.getFullName());
        response.setGender(entity.getGender());
        response.setDateOfBirth(entity.getDateOfBirth());
        response.setPhone(entity.getPhone());
        response.setAddress(entity.getAddress());
        response.setIdentityNum(entity.getIdentityNum());
        response.setMedicalHistory(entity.getMedicalHistory());
        response.setAllergy(entity.getAllergy());
        response.setIsActive(entity.getIsActive());
        if (entity.getAccount() != null) {
            response.setAccountId(entity.getAccount().getAccountId());
            response.setEmail(entity.getAccount().getEmail());
        }
        return response;
    }

    public void updateEntityFromRequest(PatientRequest request, Patient entity) {
        if (request.getFullName() != null) entity.setFullName(request.getFullName());
        if (request.getGender() != null) entity.setGender(normalizeGender(request.getGender()));
        if (request.getDateOfBirth() != null) entity.setDateOfBirth(request.getDateOfBirth());
        if (request.getPhone() != null) entity.setPhone(request.getPhone());
        if (request.getAddress() != null) entity.setAddress(request.getAddress());
        if (request.getIdentityNum() != null) entity.setIdentityNum(request.getIdentityNum());
        if (request.getMedicalHistory() != null) entity.setMedicalHistory(request.getMedicalHistory());
        if (request.getAllergy() != null) entity.setAllergy(request.getAllergy());
        // accountId is NOT updated
    }

    private String normalizeGender(String gender) {
        return gender == null || gender.isBlank()
                ? gender
                : gender.trim().toUpperCase(Locale.ROOT);
    }
}
