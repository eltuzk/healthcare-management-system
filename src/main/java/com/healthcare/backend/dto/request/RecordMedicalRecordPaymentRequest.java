package com.healthcare.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RecordMedicalRecordPaymentRequest {

    @NotNull(message = "Received amount is required")
    @DecimalMin(value = "0.01", message = "Received amount must be greater than 0")
    private BigDecimal receivedAmount;

    @Size(max = 100, message = "Receipt number must not exceed 100 characters")
    private String receiptNumber;

    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;
}
