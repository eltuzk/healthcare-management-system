package com.healthcare.backend.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientInsuranceResponse {

    private Long patientInsuranceId;
    private Long patientId;
    private String fullName;
    private String insuranceNum;
    private String status;
    private LocalDate expiryDate;
    private BigDecimal coveragePercent;
}
