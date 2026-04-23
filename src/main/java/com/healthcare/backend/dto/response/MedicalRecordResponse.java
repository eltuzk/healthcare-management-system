package com.healthcare.backend.dto.response;

import com.healthcare.backend.entity.enums.MedicalRecordConclusionType;
import com.healthcare.backend.entity.enums.MedicalRecordStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordResponse {

    private Long medicalRecordId;
    private Long appointmentId;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private Long doctorScheduleId;
    private LocalDate appointmentDate;
    private String initialDiagnosis;
    private String clinicalConclusion;
    private MedicalRecordConclusionType conclusionType;
    private String clinicalNotes;
    private String treatmentPlan;
    private MedicalRecordStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    private LocalDateTime lockedAt;
    private Long version;
}
