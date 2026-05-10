package com.healthcare.backend.mapper;

import com.healthcare.backend.dto.request.AdmissionRequestRequest;
import com.healthcare.backend.dto.response.AdmissionRequestResponse;
import com.healthcare.backend.entity.AdmissionRequest;
import com.healthcare.backend.entity.Bed;
import com.healthcare.backend.entity.MedicalRecord;
import com.healthcare.backend.entity.Patient;
import com.healthcare.backend.entity.enums.AdmissionStatus;
import org.springframework.stereotype.Component;

@Component
public class AdmissionRequestMapper {

    public AdmissionRequest toEntity(AdmissionRequestRequest request) {
        AdmissionRequest entity = new AdmissionRequest();
        entity.setAdmissionDate(request.getAdmissionDate());
        entity.setDischargeDate(request.getDischargeDate());
        entity.setStatus(AdmissionStatus.PENDING);
        return entity;
    }

    public AdmissionRequestResponse toResponse(AdmissionRequest entity) {
        AdmissionRequestResponse response = new AdmissionRequestResponse();
        response.setAdmissionId(entity.getAdmissionId());
        response.setAdmissionDate(entity.getAdmissionDate());
        response.setDischargeDate(entity.getDischargeDate());
        response.setStatus(entity.getStatus());
        response.setTotalPrice(entity.getTotalPrice());

        // Patient
        Patient patient = entity.getPatient();
        if (patient != null) {
            response.setPatientId(patient.getPatientId());
            response.setPatientFullName(patient.getFullName());
        }

        // MedicalRecord
        MedicalRecord medicalRecord = entity.getMedicalRecord();
        if (medicalRecord != null) {
            response.setMedRecordId(medicalRecord.getMedicalRecordId());
        }

        // Bed + Room
        Bed bed = entity.getBed();
        if (bed != null) {
            response.setBedId(bed.getBedId());
            if (bed.getRoom() != null) {
                response.setBedRoomCode(bed.getRoom().getRoomCode());
                response.setBedRoomPosition(bed.getRoom().getPosition());
            }
        }

        return response;
    }

}