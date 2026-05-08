package com.healthcare.backend.service.impl;

import com.healthcare.backend.dto.request.RecordMedicalRecordPaymentRequest;
import com.healthcare.backend.dto.response.PaymentRecordResponse;
import com.healthcare.backend.entity.Account;
import com.healthcare.backend.entity.MedicalRecord;
import com.healthcare.backend.entity.PaymentRecord;
import com.healthcare.backend.entity.PaymentTransaction;
import com.healthcare.backend.entity.enums.MedicalRecordStatus;
import com.healthcare.backend.entity.enums.PaymentStatus;
import com.healthcare.backend.entity.enums.PaymentTransactionStatus;
import com.healthcare.backend.exception.BusinessException;
import com.healthcare.backend.exception.ResourceNotFoundException;
import com.healthcare.backend.mapper.PaymentRecordMapper;
import com.healthcare.backend.repository.AccountRepository;
import com.healthcare.backend.repository.MedicalRecordRepository;
import com.healthcare.backend.repository.PaymentRecordRepository;
import com.healthcare.backend.repository.PaymentTransactionRepository;
import com.healthcare.backend.security.UserPrincipal;
import com.healthcare.backend.service.MedicalRecordBillingService;
import com.healthcare.backend.service.PaymentRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentRecordServiceImpl implements PaymentRecordService {

    private final PaymentRecordRepository paymentRecordRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final AccountRepository accountRepository;
    private final MedicalRecordBillingService medicalRecordBillingService;
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

    @Override
    @Transactional
    public PaymentRecordResponse recordMedicalRecordCashPayment(
            Long medicalRecordId,
            RecordMedicalRecordPaymentRequest request
    ) {
        Account currentAccount = findCurrentAccountOrThrow();
        MedicalRecord medicalRecord = medicalRecordRepository.findByIdForUpdate(medicalRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with id: " + medicalRecordId));

        if (medicalRecord.getStatus() != MedicalRecordStatus.DRAFT) {
            throw new BusinessException("Only draft medical records can be paid and moved to IN_PROGRESS");
        }

        medicalRecordBillingService.syncBilling(medicalRecordId);

        PaymentRecord paymentRecord = paymentRecordRepository.findByMedicalRecordIdForUpdate(medicalRecordId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Medical record payment record not found for id: " + medicalRecordId
                ));

        validateExactAmount(request.getReceivedAmount(), paymentRecord.getTotalPrice());

        paymentRecord.setReceivedAmount(request.getReceivedAmount());
        paymentRecord.setPaidAt(LocalDateTime.now());
        paymentRecord.setPaymentStatus(PaymentStatus.PAID);

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setPaymentRecord(paymentRecord);
        transaction.setGateway("CASH");
        transaction.setTransferType("cash");
        transaction.setTransferAmount(request.getReceivedAmount());
        transaction.setTransactionDate(paymentRecord.getPaidAt());
        transaction.setReceiptNumber(request.getReceiptNumber());
        transaction.setContent(request.getNote());
        transaction.setDescription(request.getNote());
        transaction.setRawData(request.getNote());
        transaction.setConfirmedBy(currentAccount);
        transaction.setProcessStatus(PaymentTransactionStatus.SUCCESS);

        medicalRecord.setStatus(MedicalRecordStatus.IN_PROGRESS);
        paymentTransactionRepository.save(transaction);
        paymentRecordRepository.save(paymentRecord);
        medicalRecordRepository.save(medicalRecord);

        return toResponseWithTransactions(paymentRecord);
    }

    private PaymentRecordResponse toResponseWithTransactions(PaymentRecord paymentRecord) {
        List<PaymentTransaction> transactions = paymentTransactionRepository
                .findByPaymentRecord_PaymentRecordIdOrderByTransactionDateDesc(paymentRecord.getPaymentRecordId());
        return paymentRecordMapper.toResponse(paymentRecord, transactions);
    }

    private Account findCurrentAccountOrThrow() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BusinessException("Authenticated account is required");
        }

        return accountRepository.findById(principal.accountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + principal.accountId()));
    }

    private void validateExactAmount(BigDecimal receivedAmount, BigDecimal expectedAmount) {
        if (receivedAmount == null) {
            throw new BusinessException("Received amount is required");
        }
        if (expectedAmount == null || expectedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Medical record has no payable requests");
        }
        if (receivedAmount.compareTo(expectedAmount) != 0) {
            throw new BusinessException("Received amount must match medical record total price");
        }
    }
}
