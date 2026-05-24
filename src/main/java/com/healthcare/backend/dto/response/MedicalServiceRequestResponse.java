package com.healthcare.backend.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class MedicalServiceRequestResponse {
    private Long medServiceRequestId;
    private Long medRecordId;
    private String requestCode;
    private String status;
    private String paymentStatus;
    private BigDecimal totalPrice;
    private String currency;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime paidAt;
    private String patientName;
    private List<MedicalServiceRequestItemResponse> items;
}
