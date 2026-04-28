package com.healthcare.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAppointmentRequest {

    @NotNull(message = "Patient id must not be null")
    private Long patientId;

    @NotNull(message = "Doctor schedule id must not be null")
    private Long doctorScheduleId;

    @NotBlank(message = "Initial symptoms must not be blank")
    @Size(max = 4000, message = "Initial symptoms must not exceed 4000 characters")
    private String initialSymptoms;

    @NotBlank(message = "Visit reason must not be blank")
    @Size(max = 500, message = "Visit reason must not exceed 500 characters")
    private String visitReason;
}
