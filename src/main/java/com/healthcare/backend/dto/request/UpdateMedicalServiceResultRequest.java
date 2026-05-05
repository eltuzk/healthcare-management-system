package com.healthcare.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMedicalServiceResultRequest {
    
    @NotBlank(message = "Result data cannot be blank")
    private String resultData;
}
