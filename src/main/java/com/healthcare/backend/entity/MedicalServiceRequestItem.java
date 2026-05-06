package com.healthcare.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "medical_service_request_item")
@Getter
@Setter
@NoArgsConstructor
public class MedicalServiceRequestItem {

    @EmbeddedId
    private MedicalServiceRequestItemId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("medServiceRequestId")
    @JoinColumn(name = "med_ser_req_id", nullable = false)
    private MedicalServiceRequest medicalServiceRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("medServiceId")
    @JoinColumn(name = "med_service_id", nullable = false)
    private MedicalService medicalService;

    @Column(name = "snapshot_price", precision = 15, scale = 2)
    private BigDecimal snapshotPrice;
}
