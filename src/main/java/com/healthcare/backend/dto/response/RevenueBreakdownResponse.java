package com.healthcare.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RevenueBreakdownResponse {

    private String key;
    private BigDecimal totalAmount;
    private long transactionCount;
}
