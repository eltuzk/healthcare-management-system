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
public class ConsultationFeeRequest {

    @NotBlank(message = "Fee code must not be blank")
    @Size(max = 50, message = "Fee code must not exceed 50 characters")
    private String feeCode;

    @NotBlank(message = "Fee name must not be blank")
    @Size(max = 200, message = "Fee name must not exceed 200 characters")
    private String feeName;

    @NotNull(message = "Specialty id must not be null")
    private Long specialtyId;

    @NotNull(message = "Price must not be null")
    @DecimalMin(value = "0.00", inclusive = true, message = "Price must be greater than or equal to 0")
    private BigDecimal price;
}
