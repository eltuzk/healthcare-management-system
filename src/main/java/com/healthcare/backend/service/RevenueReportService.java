package com.healthcare.backend.service;

import com.healthcare.backend.dto.response.RevenueReportResponse;
import com.healthcare.backend.entity.enums.RevenueOwnerType;

import java.time.LocalDate;

public interface RevenueReportService {

    RevenueReportResponse getRevenueReport(
            LocalDate fromDate,
            LocalDate toDate,
            String gateway,
            RevenueOwnerType ownerType
    );
}
