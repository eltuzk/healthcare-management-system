package com.healthcare.backend.dto.response;

import com.healthcare.backend.entity.enums.AdmissionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdmissionRequestResponse {

    private Long admissionId;

    private Long patientId;
    private String patientFullName;

    private Long medRecordId;

    private Long bedId;
    private String bedRoomCode;
    private String bedRoomPosition;

    private LocalDate admissionDate;
    private LocalDate dischargeDate;
    private AdmissionStatus status;
    private BigDecimal totalPrice;
}