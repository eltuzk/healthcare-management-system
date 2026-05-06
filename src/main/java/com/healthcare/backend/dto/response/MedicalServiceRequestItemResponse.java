package com.healthcare.backend.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MedicalServiceRequestItemResponse {
    private Long medServiceId;
    private String medicalServiceName;
    private BigDecimal snapshotPrice;
}
