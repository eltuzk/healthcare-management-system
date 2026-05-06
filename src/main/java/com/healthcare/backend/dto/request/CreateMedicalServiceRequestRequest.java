package com.healthcare.backend.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateMedicalServiceRequestRequest {

    @NotNull(message = "Medical Record ID is required")
    private Long medRecordId;

    private String note;

    @NotEmpty(message = "At least one medical service must be selected")
    private List<Long> medServiceIds;
}
