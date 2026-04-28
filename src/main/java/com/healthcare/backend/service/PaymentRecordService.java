package com.healthcare.backend.service;

import com.healthcare.backend.dto.response.PaymentRecordResponse;
import com.healthcare.backend.entity.enums.PaymentStatus;

import java.util.List;

public interface PaymentRecordService {

    List<PaymentRecordResponse> getAll(PaymentStatus paymentStatus, Long appointmentId, Long medicalRecordId);

    PaymentRecordResponse getById(Long paymentRecordId);
}
