package com.healthcare.backend.dto.request;

import com.healthcare.backend.entity.enums.MedicalServiceRequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMedicalServiceRequestStatusRequest {

    @NotNull(message = "Status is required")
    private MedicalServiceRequestStatus status;
}
