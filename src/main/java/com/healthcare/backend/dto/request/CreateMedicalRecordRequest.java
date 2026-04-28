package com.healthcare.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateMedicalRecordRequest {

    @NotBlank(message = "Initial diagnosis must not be blank")
    @Size(max = 1000, message = "Initial diagnosis must not exceed 1000 characters")
    private String initialDiagnosis;

    private String clinicalNotes;

    private String treatmentPlan;
}
