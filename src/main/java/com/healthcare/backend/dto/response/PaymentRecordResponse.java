package com.healthcare.backend.dto.response;

import com.healthcare.backend.entity.enums.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PaymentRecordResponse {

    private Long paymentRecordId;
    private Long appointmentId;
    private String appointmentCode;
    private Long medicalRecordId;
    private String requestCode;
    private BigDecimal totalPrice;
    private BigDecimal receivedAmount;
    private PaymentStatus paymentStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime paidAt;
    private List<PaymentTransactionResponse> transactions;
}
