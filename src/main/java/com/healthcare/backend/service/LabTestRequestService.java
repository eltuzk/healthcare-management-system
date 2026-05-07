package com.healthcare.backend.service;

import com.healthcare.backend.dto.request.CreateLabTestRequestRequest;
import com.healthcare.backend.dto.request.CreateLabTestResultRequest;
import com.healthcare.backend.dto.request.UpdateLabTestRequestStatusRequest;
import com.healthcare.backend.dto.request.UpdateLabTestResultRequest;
import com.healthcare.backend.dto.response.LabTestRequestResponse;
import com.healthcare.backend.dto.response.LabTestResultResponse;
import com.healthcare.backend.entity.enums.LabTestRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LabTestRequestService {
    
    LabTestRequestResponse createRequest(CreateLabTestRequestRequest request);

    LabTestRequestResponse getRequestById(Long id);

    Page<LabTestRequestResponse> getRequests(Long medRecordId, LabTestRequestStatus status, Pageable pageable);

    List<LabTestRequestResponse> getRequestsByMedRecord(Long medRecordId);

    LabTestRequestResponse updateStatus(Long id, UpdateLabTestRequestStatusRequest request);

    LabTestResultResponse createResult(Long requestId, CreateLabTestResultRequest request);

    LabTestResultResponse updateResult(Long resultId, UpdateLabTestResultRequest request);

    LabTestResultResponse getResultByRequestId(Long requestId);
}
