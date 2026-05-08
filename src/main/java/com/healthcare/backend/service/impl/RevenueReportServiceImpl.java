package com.healthcare.backend.service.impl;

import com.healthcare.backend.dto.response.RevenueBreakdownResponse;
import com.healthcare.backend.dto.response.RevenueReportResponse;
import com.healthcare.backend.entity.PaymentTransaction;
import com.healthcare.backend.entity.enums.PaymentTransactionStatus;
import com.healthcare.backend.entity.enums.RevenueOwnerType;
import com.healthcare.backend.exception.BusinessException;
import com.healthcare.backend.repository.PaymentTransactionRepository;
import com.healthcare.backend.service.RevenueReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RevenueReportServiceImpl implements RevenueReportService {

    private final PaymentTransactionRepository paymentTransactionRepository;

    @Override
    @Transactional(readOnly = true)
    public RevenueReportResponse getRevenueReport(
            LocalDate fromDate,
            LocalDate toDate,
            String gateway,
            RevenueOwnerType ownerType
    ) {
        LocalDate resolvedToDate = toDate != null ? toDate : LocalDate.now();
        LocalDate resolvedFromDate = fromDate != null ? fromDate : resolvedToDate.withDayOfMonth(1);

        if (resolvedFromDate.isAfter(resolvedToDate)) {
            throw new BusinessException("fromDate must be before or equal to toDate");
        }

        LocalDateTime fromDateTime = resolvedFromDate.atStartOfDay();
        LocalDateTime toDateTime = resolvedToDate.plusDays(1).atStartOfDay();

        List<PaymentTransaction> transactions = paymentTransactionRepository.findSuccessfulRevenueTransactions(
                        PaymentTransactionStatus.SUCCESS,
                        fromDateTime,
                        toDateTime,
                        normalizeGateway(gateway)
                )
                .stream()
                .filter(transaction -> ownerType == null || resolveOwnerType(transaction) == ownerType)
                .toList();

        RevenueReportResponse response = new RevenueReportResponse();
        response.setFromDate(resolvedFromDate);
        response.setToDate(resolvedToDate);
        response.setTransactionCount(transactions.size());
        response.setTotalRevenue(sumTransactions(transactions));
        response.setRevenueByDate(groupByDate(transactions));
        response.setRevenueByGateway(groupByGateway(transactions));
        response.setRevenueByOwnerType(groupByOwnerType(transactions));
        return response;
    }

    private BigDecimal sumTransactions(List<PaymentTransaction> transactions) {
        return transactions.stream()
                .map(PaymentTransaction::getTransferAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<RevenueBreakdownResponse> groupByDate(List<PaymentTransaction> transactions) {
        Map<String, RevenueAccumulator> grouped = new LinkedHashMap<>();
        transactions.stream()
                .sorted(Comparator.comparing(PaymentTransaction::getTransactionDate))
                .forEach(transaction -> addToGroup(
                        grouped,
                        transaction.getTransactionDate().toLocalDate().toString(),
                        transaction
                ));
        return toBreakdowns(grouped);
    }

    private List<RevenueBreakdownResponse> groupByGateway(List<PaymentTransaction> transactions) {
        Map<String, RevenueAccumulator> grouped = new LinkedHashMap<>();
        transactions.forEach(transaction -> addToGroup(
                grouped,
                defaultIfBlank(transaction.getGateway(), "UNKNOWN").toUpperCase(),
                transaction
        ));
        return toBreakdowns(grouped);
    }

    private List<RevenueBreakdownResponse> groupByOwnerType(List<PaymentTransaction> transactions) {
        Map<String, RevenueAccumulator> grouped = new LinkedHashMap<>();
        transactions.forEach(transaction -> addToGroup(
                grouped,
                resolveOwnerType(transaction).name(),
                transaction
        ));
        return toBreakdowns(grouped);
    }

    private void addToGroup(
            Map<String, RevenueAccumulator> grouped,
            String key,
            PaymentTransaction transaction
    ) {
        grouped.computeIfAbsent(key, ignored -> new RevenueAccumulator())
                .add(transaction.getTransferAmount());
    }

    private List<RevenueBreakdownResponse> toBreakdowns(Map<String, RevenueAccumulator> grouped) {
        return grouped.entrySet()
                .stream()
                .map(entry -> new RevenueBreakdownResponse(
                        entry.getKey(),
                        entry.getValue().totalAmount(),
                        entry.getValue().transactionCount()
                ))
                .toList();
    }

    private RevenueOwnerType resolveOwnerType(PaymentTransaction transaction) {
        if (transaction.getPaymentRecord().getAppointment() != null) {
            return RevenueOwnerType.APPOINTMENT;
        }
        return RevenueOwnerType.MEDICAL_RECORD;
    }

    private String normalizeGateway(String gateway) {
        return gateway == null || gateway.isBlank() ? null : gateway.trim();
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static class RevenueAccumulator {
        private BigDecimal totalAmount = BigDecimal.ZERO;
        private long transactionCount;

        void add(BigDecimal amount) {
            if (amount != null) {
                totalAmount = totalAmount.add(amount);
            }
            transactionCount++;
        }

        BigDecimal totalAmount() {
            return totalAmount;
        }

        long transactionCount() {
            return transactionCount;
        }
    }
}
