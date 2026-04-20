package com.healthcare.backend.dto.request;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
@Getter
@Setter
public class BedRequest {

    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0", message = "Giá trị tối thiểu là 0")
    private BigDecimal price;
}



