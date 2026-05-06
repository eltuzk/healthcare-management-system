package com.healthcare.backend.service;

import com.healthcare.backend.dto.request.CreateMedicalServiceRequestRequest;
import com.healthcare.backend.dto.request.UpdateMedicalServiceRequestStatusRequest;
import com.healthcare.backend.dto.request.UpdateMedicalServiceResultRequest;
import com.healthcare.backend.dto.response.MedicalServiceRequestResponse;
import com.healthcare.backend.dto.response.MedicalServiceResultResponse;
import com.healthcare.backend.entity.enums.MedicalServiceRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MedicalServiceRequestService {
    
    MedicalServiceRequestResponse createRequest(CreateMedicalServiceRequestRequest request);
    
    MedicalServiceRequestResponse getRequestById(Long id);
    
    Page<MedicalServiceRequestResponse> getRequests(Long medRecordId, MedicalServiceRequestStatus status, Pageable pageable);
    
    MedicalServiceRequestResponse updateStatus(Long id, UpdateMedicalServiceRequestStatusRequest request);
    
    MedicalServiceResultResponse createResult(Long requestId, UpdateMedicalServiceResultRequest request);
    
    MedicalServiceResultResponse updateResult(Long resultId, UpdateMedicalServiceResultRequest request);
    
    MedicalServiceResultResponse getResultByRequestId(Long requestId);
}
