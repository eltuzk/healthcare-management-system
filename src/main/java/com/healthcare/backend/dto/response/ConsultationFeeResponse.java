package com.healthcare.backend.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ConsultationFeeResponse {

    private Long feeId;
    private String feeCode;
    private String feeName;
    private String specialty;
    private Long specialtyId;
    private String specialtyCode;
    private String specialtyName;
    private BigDecimal price;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
