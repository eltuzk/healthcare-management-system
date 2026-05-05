package com.healthcare.backend.dto.response;

import com.healthcare.backend.entity.enums.PaymentTransactionStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentTransactionResponse {

    private Long transactionId;
    private String transferType;
    private String gateway;
    private String accountNumber;
    private String sepayTransactionId;
    private BigDecimal transferAmount;
    private LocalDateTime transactionDate;
    private String referenceCode;
    private String content;
    private String description;
    private String receiptNumber;
    private Long confirmedByAccountId;
    private String confirmedByEmail;
    private PaymentTransactionStatus processStatus;
}
