package com.healthcare.backend.service.impl;

import com.healthcare.backend.dto.response.PaymentRecordResponse;
import com.healthcare.backend.entity.PaymentRecord;
import com.healthcare.backend.entity.PaymentTransaction;
import com.healthcare.backend.entity.enums.PaymentStatus;
import com.healthcare.backend.exception.ResourceNotFoundException;
import com.healthcare.backend.mapper.PaymentRecordMapper;
import com.healthcare.backend.repository.PaymentRecordRepository;
import com.healthcare.backend.repository.PaymentTransactionRepository;
import com.healthcare.backend.service.PaymentRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentRecordServiceImpl implements PaymentRecordService {

    private final PaymentRecordRepository paymentRecordRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentRecordMapper paymentRecordMapper;

    @Override
    // readOnly giúp Hibernate không dirty-check không cần thiết; API payment record chỉ phục vụ xem dữ liệu kế toán.
    @Transactional(readOnly = true)
    public List<PaymentRecordResponse> getAll(PaymentStatus paymentStatus, Long appointmentId, Long medicalRecordId) {
        return paymentRecordRepository.findAllByFilters(paymentStatus, appointmentId, medicalRecordId)
                .stream()
                .sorted(Comparator.comparing(PaymentRecord::getCreatedAt).reversed())
                .map(this::toResponseWithTransactions)
                .toList();
    }

    @Override
    // readOnly vì payment record/transaction được sinh bởi business flow, không sửa trực tiếp từ API này.
    @Transactional(readOnly = true)
    public PaymentRecordResponse getById(Long paymentRecordId) {
        PaymentRecord paymentRecord = paymentRecordRepository.findById(paymentRecordId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment record not found with id: " + paymentRecordId
                ));
        return toResponseWithTransactions(paymentRecord);
    }

    private PaymentRecordResponse toResponseWithTransactions(PaymentRecord paymentRecord) {
        List<PaymentTransaction> transactions = paymentTransactionRepository
                .findByPaymentRecord_PaymentRecordIdOrderByTransactionDateDesc(paymentRecord.getPaymentRecordId());
        return paymentRecordMapper.toResponse(paymentRecord, transactions);
    }
}
