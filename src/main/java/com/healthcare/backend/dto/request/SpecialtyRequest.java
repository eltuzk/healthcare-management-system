package com.healthcare.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpecialtyRequest {

    @NotBlank(message = "Specialty code must not be blank")
    @Size(max = 50, message = "Specialty code must not exceed 50 characters")
    private String specialtyCode;

    @NotBlank(message = "Specialty name must not be blank")
    @Size(max = 200, message = "Specialty name must not exceed 200 characters")
    private String specialtyName;
}
