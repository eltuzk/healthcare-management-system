package com.healthcare.backend.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MedicalServiceResponse {

    private Long medServiceId;

    private String medicalServiceName;

    private BigDecimal price;

    private boolean active;
}