package com.healthcare.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrescriptionDetailRequest {

    @NotNull(message = "Medicine id must not be null")
    private Long medicineId;

    @NotBlank(message = "Dosage must not be blank")
    @Size(max = 100, message = "Dosage must not exceed 100 characters")
    private String dosage;

    @NotBlank(message = "Frequency must not be blank")
    @Size(max = 100, message = "Frequency must not exceed 100 characters")
    private String frequency;

    @NotBlank(message = "Duration must not be blank")
    @Size(max = 100, message = "Duration must not exceed 100 characters")
    private String duration;

    @NotNull(message = "Quantity must not be null")
    @Min(value = 1, message = "Quantity must be greater than or equal to 1")
    private Integer quantity;

    @Size(max = 500, message = "Instruction must not exceed 500 characters")
    private String instruction;
}