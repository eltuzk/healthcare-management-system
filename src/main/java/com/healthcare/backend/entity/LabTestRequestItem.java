package com.healthcare.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "lab_test_request_item")
@Getter
@Setter
@NoArgsConstructor
public class LabTestRequestItem {

    @EmbeddedId
    private LabTestRequestItemId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("labTestRequestId")
    @JoinColumn(name = "lab_test_request_id", nullable = false)
    private LabTestRequest labTestRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("labTestId")
    @JoinColumn(name = "lab_test_id", nullable = false)
    private LabTest labTest;

    @Column(name = "snapshot_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal snapshotPrice;

    public LabTestRequestItem(LabTestRequest labTestRequest, LabTest labTest, BigDecimal snapshotPrice) {
        this.labTestRequest = labTestRequest;
        this.labTest = labTest;
        this.snapshotPrice = snapshotPrice;
        this.id = new LabTestRequestItemId(labTestRequest.getLabTestRequestId(), labTest.getLabTestId());
    }
}
