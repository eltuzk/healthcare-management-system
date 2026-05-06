package com.healthcare.backend.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MedicalServiceResultResponse {
    private Long medServiceResultId;
    private Long medServiceRequestId;
    private String resultData;
    private LocalDateTime createdAt;
}
