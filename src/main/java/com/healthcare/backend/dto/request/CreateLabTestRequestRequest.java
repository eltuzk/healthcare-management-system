package com.healthcare.backend.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateLabTestRequestRequest {

    @NotNull(message = "Medical Record ID is required")
    private Long medRecordId;

    private String note;

    @NotEmpty(message = "At least one Lab Test must be selected")
    private List<Long> labTestIds;
}
