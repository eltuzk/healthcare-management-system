package com.healthcare.backend.mapper;

import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.healthcare.backend.dto.request.PatientRequest;
import com.healthcare.backend.dto.response.PatientResponse;
import com.healthcare.backend.entity.Patient;

@Component
public class PatientMapper {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    public PatientResponse toDto(Patient patient) {
        if(patient == null) return null;

        PatientResponse response = new PatientResponse();
        response.setPatientId(patient.getPatientId());
        response.setFullName(patient.getFullName());
        response.setGender(patient.getGender());
        response.setPhone(patient.getPhone());
        response.setAddress(patient.getAddress());
        response.setIdentityNum(patient.getIdentityNum());
        response.setMedicalHistory(patient.getMedicalHistory());
        response.setAllergy(patient.getAllergy());
        response.setIsActive(patient.getIsActive());

        if (patient.getAccount() != null) {
            response.setAccountEmail(patient.getAccount().getEmail());
        }

        if (patient.getDateOfBirth() != null) {
            response.setDateOfBirth(patient.getDateOfBirth().format(FORMATTER));
        }

        return response;
    }
    
    public Patient createEntityFromDto(PatientRequest request) {
        if (request == null) return null;

        Patient patient = new Patient();
        
        patient.setFullName(request.getFullName());
        patient.setGender(request.getGender());
        patient.setPhone(request.getPhone());
        patient.setAddress(request.getAddress());
        patient.setIdentityNum(request.getIdentityNum());
        patient.setAllergy(request.getAllergy());
        patient.setIsActive(request.getIsActive());
        patient.setMedicalHistory(request.getMedicalHistory());
        patient.setDateOfBirth(request.getDateOfBirth());

        return patient;
    }

    public void updatePatientFromDto(Patient patient, PatientRequest request) {
        if (request == null || patient == null) return;

        patient.setFullName(request.getFullName());
        patient.setGender(request.getGender());
        patient.setPhone(request.getPhone());
        patient.setAddress(request.getAddress());
        patient.setAllergy(request.getAllergy());
        patient.setIsActive(request.getIsActive());
        patient.setMedicalHistory(request.getMedicalHistory());
        patient.setIdentityNum(request.getIdentityNum());
        patient.setDateOfBirth(request.getDateOfBirth());
    }
}
