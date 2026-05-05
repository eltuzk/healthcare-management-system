package com.healthcare.backend.dto.request;

import com.healthcare.backend.entity.enums.LabTestRequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateLabTestRequestStatusRequest {

    @NotNull(message = "Status is required")
    private LabTestRequestStatus status;
}
