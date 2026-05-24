package com.healthcare.backend.dto.response;

import com.healthcare.backend.entity.enums.LabTestRequestStatus;
import com.healthcare.backend.entity.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabTestRequestResponse {
    private Long labTestRequestId;
    private Long medRecordId;
    private String requestCode;
    private LabTestRequestStatus status;
    private PaymentStatus paymentStatus;
    private BigDecimal totalPrice;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime paidAt;
    private String patientName;
    private List<LabTestRequestItemResponse> items;
}
