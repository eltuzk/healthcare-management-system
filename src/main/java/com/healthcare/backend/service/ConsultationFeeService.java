package com.healthcare.backend.service;

import com.healthcare.backend.dto.request.ConsultationFeeRequest;
import com.healthcare.backend.dto.response.ConsultationFeeResponse;

import java.util.List;

public interface ConsultationFeeService {

    ConsultationFeeResponse create(ConsultationFeeRequest request);

    List<ConsultationFeeResponse> getAll();

    ConsultationFeeResponse getById(Long feeId);

    ConsultationFeeResponse update(Long feeId, ConsultationFeeRequest request);

    ConsultationFeeResponse deactivate(Long feeId);
}
