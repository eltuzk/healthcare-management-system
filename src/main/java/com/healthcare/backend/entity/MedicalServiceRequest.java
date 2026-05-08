package com.healthcare.backend.entity;

import com.healthcare.backend.entity.enums.MedicalServiceRequestStatus;
import com.healthcare.backend.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "medical_service_request")
@Getter
@Setter
@NoArgsConstructor
public class MedicalServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "med_ser_req_id")
    private Long medServiceRequestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "med_record_id", nullable = false)
    private MedicalRecord medRecord;

    @Column(name = "request_code", nullable = false, unique = true, length = 100)
    private String requestCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MedicalServiceRequestStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column(name = "total_price", precision = 15, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency = "VND";

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @OneToMany(mappedBy = "medicalServiceRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MedicalServiceRequestItem> items = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = MedicalServiceRequestStatus.NOT_COLLECTED;
        }
        if (this.paymentStatus == null) {
            this.paymentStatus = PaymentStatus.UNPAID;
        }
        if (this.currency == null) {
            this.currency = "VND";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
