package com.healthcare.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabTestRequestItemResponse {
    private Long labTestId;
    private String labTestName;
    private BigDecimal snapshotPrice;
}
