package com.healthcare.backend.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class MedicineLotResponse {

    private Long medicineLotId;

    private Long medicineId;

    private String medicineName;

    private String lotNumber;

    private LocalDate manufacturingDate;

    private LocalDate expiryDate;

    private Integer quantity;

    private BigDecimal importPrice;

    private boolean active;
}