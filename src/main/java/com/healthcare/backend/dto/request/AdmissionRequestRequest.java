package com.healthcare.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdmissionRequestRequest {

    @NotNull(message = "PatientId không được để trống")
    private Long patientId;

    @NotNull(message = "MedRecordId không được để trống")
    private Long medRecordId;

    @NotNull(message = "BedId không được để trống")
    private Long bedId;

    @NotNull(message = "Ngày nhập viện không được để trống")
    private LocalDate admissionDate;

    private LocalDate dischargeDate;
}