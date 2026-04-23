package com.healthcare.backend.mapper;

import com.healthcare.backend.dto.response.PaymentRecordResponse;
import com.healthcare.backend.dto.response.PaymentTransactionResponse;
import com.healthcare.backend.entity.PaymentRecord;
import com.healthcare.backend.entity.PaymentTransaction;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentRecordMapper {

    public PaymentRecordResponse toResponse(PaymentRecord paymentRecord, List<PaymentTransaction> transactions) {
        if (paymentRecord == null) {
            return null;
        }

        PaymentRecordResponse response = new PaymentRecordResponse();
        response.setPaymentRecordId(paymentRecord.getPaymentRecordId());
        response.setRequestCode(paymentRecord.getRequestCode());
        response.setTotalPrice(paymentRecord.getTotalPrice());
        response.setReceivedAmount(paymentRecord.getReceivedAmount());
        response.setPaymentStatus(paymentRecord.getPaymentStatus());
        response.setCreatedAt(paymentRecord.getCreatedAt());
        response.setUpdatedAt(paymentRecord.getUpdatedAt());
        response.setPaidAt(paymentRecord.getPaidAt());

        if (paymentRecord.getAppointment() != null) {
            response.setAppointmentId(paymentRecord.getAppointment().getAppointmentId());
            response.setAppointmentCode(paymentRecord.getAppointment().getAppointmentCode());
        }

        if (paymentRecord.getMedicalRecord() != null) {
            response.setMedicalRecordId(paymentRecord.getMedicalRecord().getMedicalRecordId());
        }

        response.setTransactions(transactions == null
                ? List.of()
                : transactions.stream().map(this::toTransactionResponse).toList());

        return response;
    }

    private PaymentTransactionResponse toTransactionResponse(PaymentTransaction transaction) {
        PaymentTransactionResponse response = new PaymentTransactionResponse();
        response.setTransactionId(transaction.getTransactionId());
        response.setTransferType(transaction.getTransferType());
        response.setGateway(transaction.getGateway());
        response.setAccountNumber(transaction.getAccountNumber());
        response.setSepayTransactionId(transaction.getSepayTransactionId());
        response.setTransferAmount(transaction.getTransferAmount());
        response.setTransactionDate(transaction.getTransactionDate());
        response.setReferenceCode(transaction.getReferenceCode());
        response.setContent(transaction.getContent());
        response.setDescription(transaction.getDescription());
        response.setReceiptNumber(transaction.getReceiptNumber());
        response.setProcessStatus(transaction.getProcessStatus());

        if (transaction.getConfirmedBy() != null) {
            response.setConfirmedByAccountId(transaction.getConfirmedBy().getAccountId());
            response.setConfirmedByEmail(transaction.getConfirmedBy().getEmail());
        }

        return response;
    }
}
