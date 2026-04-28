package com.healthcare.backend.mapper;

import com.healthcare.backend.dto.request.CreateMedicalRecordRequest;
import com.healthcare.backend.dto.request.UpdateMedicalRecordRequest;
import com.healthcare.backend.dto.response.MedicalRecordResponse;
import com.healthcare.backend.entity.MedicalRecord;
import org.springframework.stereotype.Component;

@Component
public class MedicalRecordMapper {

    public MedicalRecord toEntity(CreateMedicalRecordRequest request) {
        if (request == null) {
            return null;
        }

        MedicalRecord medicalRecord = new MedicalRecord();
        medicalRecord.setInitialDiagnosis(request.getInitialDiagnosis());
        medicalRecord.setClinicalNotes(request.getClinicalNotes());
        medicalRecord.setTreatmentPlan(request.getTreatmentPlan());
        return medicalRecord;
    }

    public void updateEntityFromRequest(UpdateMedicalRecordRequest request, MedicalRecord medicalRecord) {
        if (request == null || medicalRecord == null) {
            return;
        }

        if (request.getInitialDiagnosis() != null) {
            medicalRecord.setInitialDiagnosis(request.getInitialDiagnosis());
        }
        if (request.getClinicalConclusion() != null) {
            medicalRecord.setClinicalConclusion(request.getClinicalConclusion());
        }
        if (request.getConclusionType() != null) {
            medicalRecord.setConclusionType(request.getConclusionType());
        }
        if (request.getClinicalNotes() != null) {
            medicalRecord.setClinicalNotes(request.getClinicalNotes());
        }
        if (request.getTreatmentPlan() != null) {
            medicalRecord.setTreatmentPlan(request.getTreatmentPlan());
        }
    }

    public MedicalRecordResponse toResponse(MedicalRecord medicalRecord) {
        if (medicalRecord == null) {
            return null;
        }

        MedicalRecordResponse response = new MedicalRecordResponse();
        response.setMedicalRecordId(medicalRecord.getMedicalRecordId());
        response.setInitialDiagnosis(medicalRecord.getInitialDiagnosis());
        response.setClinicalConclusion(medicalRecord.getClinicalConclusion());
        response.setConclusionType(medicalRecord.getConclusionType());
        response.setClinicalNotes(medicalRecord.getClinicalNotes());
        response.setTreatmentPlan(medicalRecord.getTreatmentPlan());
        response.setStatus(medicalRecord.getStatus());
        response.setCreatedAt(medicalRecord.getCreatedAt());
        response.setUpdatedAt(medicalRecord.getUpdatedAt());
        response.setCompletedAt(medicalRecord.getCompletedAt());
        response.setLockedAt(medicalRecord.getLockedAt());
        response.setVersion(medicalRecord.getVersionNumber());

        if (medicalRecord.getAppointment() != null) {
            response.setAppointmentId(medicalRecord.getAppointment().getAppointmentId());

            if (medicalRecord.getAppointment().getDoctorSchedule() != null) {
                response.setDoctorScheduleId(medicalRecord.getAppointment().getDoctorSchedule().getDoctorScheduleId());
                response.setAppointmentDate(medicalRecord.getAppointment().getDoctorSchedule().getScheduleDate());
            }
        }

        if (medicalRecord.getPatient() != null) {
            response.setPatientId(medicalRecord.getPatient().getPatientId());
            response.setPatientName(medicalRecord.getPatient().getFullName());
        }

        if (medicalRecord.getDoctor() != null) {
            response.setDoctorId(medicalRecord.getDoctor().getDoctorId());
            response.setDoctorName(medicalRecord.getDoctor().getFullName());
        }

        return response;
    }
}
