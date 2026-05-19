package com.healthcare.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PrescriptionRequest {

    @NotNull(message = "Medical record id must not be null")
    private Long medicalRecordId;

    @Size(max = 1000, message = "Note must not exceed 1000 characters")
    private String note;

    @Valid
    @NotEmpty(message = "Prescription details must not be empty")
    private List<PrescriptionDetailRequest> details;
}