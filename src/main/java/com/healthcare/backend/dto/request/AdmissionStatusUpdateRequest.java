package com.healthcare.backend.dto.request;

import com.healthcare.backend.entity.enums.AdmissionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdmissionStatusUpdateRequest {

    @NotNull(message = "Status không được để trống")
    private AdmissionStatus status;

    private LocalDate dischargeDate;
}