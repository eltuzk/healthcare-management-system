package com.healthcare.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MedicineRequest {

    @NotBlank(message = "Medicine name must not be blank")
    @Size(max = 200, message = "Medicine name must not exceed 200 characters")
    private String medicineName;

    @Size(max = 200, message = "Active ingredient must not exceed 200 characters")
    private String activeIngredient;

    @NotBlank(message = "Unit must not be blank")
    @Size(max = 50, message = "Unit must not exceed 50 characters")
    private String unit;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}