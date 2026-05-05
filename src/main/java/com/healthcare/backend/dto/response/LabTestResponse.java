package com.healthcare.backend.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class LabTestResponse {

    private Long labTestId;

    private String labTestName;

    private BigDecimal price;

    private boolean active;
}