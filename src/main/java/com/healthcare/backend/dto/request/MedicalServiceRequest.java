package com.healthcare.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MedicalServiceRequest {

    @NotBlank(message = "Medical service name must not be blank")
    @Size(max = 200, message = "Medical service name must not exceed 200 characters")
    private String medicalServiceName;

    @NotNull(message = "Price must not be null")
    @DecimalMin(value = "0.00", message = "Price must be greater than or equal to 0")
    private BigDecimal price;
}