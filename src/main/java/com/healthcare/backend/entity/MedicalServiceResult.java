package com.healthcare.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "medical_service_result")
@Getter
@Setter
@NoArgsConstructor
public class MedicalServiceResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "med_service_result_id")
    private Long medServiceResultId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "med_ser_req_id", nullable = false, unique = true)
    private MedicalServiceRequest medicalServiceRequest;

    @Lob
    @Column(name = "result_data")
    private String resultData;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
