package com.healthcare.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdmissionRecordRequest {

    @NotBlank(message = "Huyết áp không được để trống")
    private String bloodPressure;

    @NotNull(message = "Nhịp tim không được để trống")
    private Integer heartRate;

    @NotNull(message = "Nhiệt độ không được để trống")
    private Double temperature;
}