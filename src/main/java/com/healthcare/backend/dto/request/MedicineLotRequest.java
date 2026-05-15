package com.healthcare.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class MedicineLotRequest {

    @NotNull(message = "Medicine id must not be null")
    private Long medicineId;

    @NotBlank(message = "Lot number must not be blank")
    @Size(max = 100, message = "Lot number must not exceed 100 characters")
    private String lotNumber;

    private LocalDate manufacturingDate;

    @NotNull(message = "Expiry date must not be null")
    private LocalDate expiryDate;

    @NotNull(message = "Quantity must not be null")
    @Min(value = 0, message = "Quantity must be greater than or equal to 0")
    private Integer quantity;

    @DecimalMin(value = "0.00", message = "Import price must be greater than or equal to 0")
    private BigDecimal importPrice;
}
