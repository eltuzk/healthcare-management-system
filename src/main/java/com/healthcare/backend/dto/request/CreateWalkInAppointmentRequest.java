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
public class CreateWalkInAppointmentRequest {

    @NotNull(message = "Patient id must not be null")
    private Long patientId;

    @NotNull(message = "Doctor schedule id must not be null")
    private Long doctorScheduleId;

    @NotNull(message = "Received amount must not be null")
    @DecimalMin(value = "0.00", inclusive = false, message = "Received amount must be greater than zero")
    private BigDecimal receivedAmount;

    @NotBlank(message = "Initial symptoms must not be blank")
    @Size(max = 4000, message = "Initial symptoms must not exceed 4000 characters")
    private String initialSymptoms;

    @NotBlank(message = "Visit reason must not be blank")
    @Size(max = 500, message = "Visit reason must not exceed 500 characters")
    private String visitReason;

    @Size(max = 100, message = "Receipt number must not exceed 100 characters")
    private String receiptNumber;

    @Size(max = 1000, message = "Note must not exceed 1000 characters")
    private String note;
}
