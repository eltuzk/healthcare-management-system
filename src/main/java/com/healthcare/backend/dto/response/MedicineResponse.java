package com.healthcare.backend.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MedicineResponse {

    private Long medicineId;

    private String medicineName;

    private String activeIngredient;

    private String unit;

    private String description;

    private boolean active;

    private java.math.BigDecimal sellingPrice;
}