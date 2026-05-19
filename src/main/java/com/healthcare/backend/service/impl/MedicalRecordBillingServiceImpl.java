package com.healthcare.backend.service.impl;

import com.healthcare.backend.entity.MedicalRecord;
import com.healthcare.backend.entity.PaymentRecord;
import com.healthcare.backend.entity.enums.PaymentStatus;
import com.healthcare.backend.exception.ResourceNotFoundException;
import com.healthcare.backend.repository.LabTestRequestRepository;
import com.healthcare.backend.repository.MedicalRecordRepository;
import com.healthcare.backend.repository.MedicalServiceRequestRepository;
import com.healthcare.backend.repository.PaymentRecordRepository;
import com.healthcare.backend.service.MedicalRecordBillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class MedicalRecordBillingServiceImpl implements MedicalRecordBillingService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final LabTestRequestRepository labTestRequestRepository;
    private final MedicalServiceRequestRepository medicalServiceRequestRepository;
    private final PaymentRecordRepository paymentRecordRepository;

    @Override
    @Transactional
    public void initializePaymentRecord(MedicalRecord medicalRecord) {
        if (medicalRecord == null || medicalRecord.getMedicalRecordId() == null) {
            return;
        }

        paymentRecordRepository.findByMedicalRecord_MedicalRecordId(medicalRecord.getMedicalRecordId())
                .orElseGet(() -> {
                    PaymentRecord paymentRecord = new PaymentRecord();
                    paymentRecord.setMedicalRecord(medicalRecord);
                    paymentRecord.setRequestCode(buildRequestCode(medicalRecord.getMedicalRecordId()));
                    paymentRecord.setTotalPrice(defaultAmount(medicalRecord.getTotalPrice()));
                    paymentRecord.setReceivedAmount(BigDecimal.ZERO);
                    paymentRecord.setPaymentStatus(PaymentStatus.UNPAID);
                    return paymentRecordRepository.save(paymentRecord);
                });
    }

    @Override
    @Transactional
    public void syncBilling(Long medicalRecordId) {
        MedicalRecord medicalRecord = medicalRecordRepository.findByIdForUpdate(medicalRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with id: " + medicalRecordId));

        BigDecimal totalPrice = sumActiveLabTestRequests(medicalRecordId)
                .add(sumActiveMedicalServiceRequests(medicalRecordId));
        medicalRecord.setTotalPrice(totalPrice);
        medicalRecordRepository.save(medicalRecord);

        PaymentRecord paymentRecord = paymentRecordRepository.findByMedicalRecordIdForUpdate(medicalRecordId)
                .orElseGet(() -> {
                    PaymentRecord created = new PaymentRecord();
                    created.setMedicalRecord(medicalRecord);
                    created.setRequestCode(buildRequestCode(medicalRecordId));
                    created.setReceivedAmount(BigDecimal.ZERO);
                    return created;
                });

        paymentRecord.setTotalPrice(totalPrice);
        paymentRecord.setPaymentStatus(resolvePaymentStatus(totalPrice, paymentRecord.getReceivedAmount()));
        paymentRecordRepository.save(paymentRecord);
    }

    private BigDecimal sumActiveLabTestRequests(Long medicalRecordId) {
        return defaultAmount(labTestRequestRepository.sumTotalPriceByMedicalRecordId(medicalRecordId));
    }

    private BigDecimal sumActiveMedicalServiceRequests(Long medicalRecordId) {
        return defaultAmount(medicalServiceRequestRepository.sumTotalPriceByMedicalRecordId(medicalRecordId));
    }

    private PaymentStatus resolvePaymentStatus(BigDecimal totalPrice, BigDecimal receivedAmount) {
        BigDecimal expected = defaultAmount(totalPrice);
        BigDecimal received = defaultAmount(receivedAmount);

        if (expected.compareTo(BigDecimal.ZERO) == 0 || received.compareTo(BigDecimal.ZERO) == 0) {
            return PaymentStatus.UNPAID;
        }

        return received.compareTo(expected) < 0 ? PaymentStatus.PARTIAL : PaymentStatus.PAID;
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount != null ? amount : BigDecimal.ZERO;
    }

    private String buildRequestCode(Long medicalRecordId) {
        return "MR-" + medicalRecordId;
    }
}
