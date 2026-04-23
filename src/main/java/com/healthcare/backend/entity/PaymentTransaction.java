package com.healthcare.backend.entity;

import com.healthcare.backend.entity.enums.PaymentTransactionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transaction")
@Getter
@Setter
@NoArgsConstructor
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_record_id", nullable = false)
    private PaymentRecord paymentRecord;

    @Column(name = "transfer_type", length = 50)
    private String transferType;

    @Column(name = "gateway", nullable = false, length = 50)
    private String gateway;

    @Column(name = "account_number", length = 100)
    private String accountNumber;

    @Column(name = "sepay_transaction_id", unique = true, length = 200)
    private String sepayTransactionId;

    @Column(name = "transfer_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal transferAmount;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "reference_code", length = 200)
    private String referenceCode;

    @Column(name = "content", length = 500)
    private String content;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "receipt_number", length = 100)
    private String receiptNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confirmed_by_account_id")
    private Account confirmedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "process_status", nullable = false, length = 20)
    private PaymentTransactionStatus processStatus;

    @Lob
    @Column(name = "raw_data")
    private String rawData;

    @PrePersist
    public void prePersist() {
        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }
        if (processStatus == null) {
            processStatus = PaymentTransactionStatus.PENDING;
        }
    }
}
