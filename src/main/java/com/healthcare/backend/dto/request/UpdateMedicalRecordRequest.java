package com.healthcare.backend.dto.request;

import com.healthcare.backend.entity.enums.MedicalRecordConclusionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMedicalRecordRequest {

    @NotNull(message = "Version must not be null")
    private Long version;

    @Size(max = 1000, message = "Initial diagnosis must not exceed 1000 characters")
    private String initialDiagnosis;

    private String clinicalConclusion;

    private MedicalRecordConclusionType conclusionType;

    private String clinicalNotes;

    private String treatmentPlan;
}
