package com.healthcare.backend.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdmissionRecordResponse {

    private Long admissionRecordId;
    private Long admissionId;
    private String bloodPressure;
    private Integer heartRate;
    private Double temperature;
    private LocalDateTime recordDate;
}