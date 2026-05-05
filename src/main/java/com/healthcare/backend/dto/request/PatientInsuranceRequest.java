package com.healthcare.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientInsuranceRequest {

    @NotNull(message = "Patient ID không được để trống")
    private Long patientId;

    @NotBlank(message = "Số bảo hiểm không được để trống")
    private String insuranceNum;

    @NotNull(message = "Tỷ lệ thanh toán không được để trống")
    @DecimalMin(value = "0", message = "Tỷ lệ thanh toán phải >= 0")
    @DecimalMax(value = "100", message = "Tỷ lệ thanh toán phải <= 100")
    private BigDecimal coveragePercent;

    @NotNull(message = "Ngày hết hạn không được để trống")
    private LocalDate expiryDate;

    private String status;
}
