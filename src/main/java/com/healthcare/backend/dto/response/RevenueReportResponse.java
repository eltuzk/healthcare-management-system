package com.healthcare.backend.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class RevenueReportResponse {

    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal totalRevenue;
    private BigDecimal totalExpense;
    private BigDecimal netProfit;
    private long transactionCount;
    private List<RevenueBreakdownResponse> revenueByDate;
    private List<RevenueBreakdownResponse> revenueByGateway;
    private List<RevenueBreakdownResponse> revenueByOwnerType;
}



