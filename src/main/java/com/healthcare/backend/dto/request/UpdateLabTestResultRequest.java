package com.healthcare.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateLabTestResultRequest {

    @NotBlank(message = "Result data is required")
    private String resultData;
}
