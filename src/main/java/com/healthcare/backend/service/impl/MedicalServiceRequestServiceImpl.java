package com.healthcare.backend.service.impl;

import com.healthcare.backend.dto.request.CreateMedicalServiceRequestRequest;
import com.healthcare.backend.dto.request.UpdateMedicalServiceRequestStatusRequest;
import com.healthcare.backend.dto.request.UpdateMedicalServiceResultRequest;
import com.healthcare.backend.dto.response.MedicalServiceRequestItemResponse;
import com.healthcare.backend.dto.response.MedicalServiceRequestResponse;
import com.healthcare.backend.dto.response.MedicalServiceResultResponse;
import com.healthcare.backend.entity.MedicalRecord;
import com.healthcare.backend.entity.MedicalService;
import com.healthcare.backend.entity.MedicalServiceRequest;
import com.healthcare.backend.entity.MedicalServiceRequestItem;
import com.healthcare.backend.entity.MedicalServiceRequestItemId;
import com.healthcare.backend.entity.MedicalServiceResult;
import com.healthcare.backend.entity.enums.MedicalRecordStatus;
import com.healthcare.backend.entity.enums.MedicalServiceRequestStatus;
import com.healthcare.backend.exception.BusinessException;
import com.healthcare.backend.exception.ResourceNotFoundException;
import com.healthcare.backend.repository.MedicalRecordRepository;
import com.healthcare.backend.repository.MedicalServiceRepository;
import com.healthcare.backend.repository.MedicalServiceRequestRepository;
import com.healthcare.backend.repository.MedicalServiceResultRepository;
import com.healthcare.backend.service.MedicalServiceRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalServiceRequestServiceImpl implements MedicalServiceRequestService {

    private final MedicalServiceRequestRepository requestRepository;
    private final MedicalServiceResultRepository resultRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final MedicalServiceRepository medicalServiceRepository;

    @Override
    @Transactional
    public MedicalServiceRequestResponse createRequest(CreateMedicalServiceRequestRequest requestDto) {
        MedicalRecord medRecord = medicalRecordRepository.findById(requestDto.getMedRecordId())
                .orElseThrow(() -> new ResourceNotFoundException("Medical Record not found with id: " + requestDto.getMedRecordId()));

        if (medRecord.getStatus() == MedicalRecordStatus.LOCKED) {
            throw new BusinessException("Cannot create request for locked medical record");
        }

        if (requestDto.getMedServiceIds() == null || requestDto.getMedServiceIds().isEmpty()) {
            throw new BusinessException("At least one medical service must be selected");
        }

        MedicalServiceRequest request = new MedicalServiceRequest();
        request.setMedRecord(medRecord);
        request.setNote(requestDto.getNote());
        request.setRequestCode(UUID.randomUUID().toString().substring(0, 10).toUpperCase());
        
        // 1. First save request without items to get the generated ID
        request.setItems(new ArrayList<>());
        request = requestRepository.save(request);
        
        List<MedicalServiceRequestItem> items = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (Long serviceId : requestDto.getMedServiceIds()) {
            MedicalService service = medicalServiceRepository.findById(serviceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Medical Service not found with id: " + serviceId));

            if (service.getIsActive() == 0) {
                throw new BusinessException("Medical service " + service.getMedicalServiceName() + " is not active");
            }

            MedicalServiceRequestItem item = new MedicalServiceRequestItem();
            // Now we have request ID, so we can set the composite ID
            item.setId(new MedicalServiceRequestItemId(request.getMedServiceRequestId(), service.getMedServiceId()));
            item.setMedicalServiceRequest(request);
            item.setMedicalService(service);
            item.setSnapshotPrice(service.getPrice());

            items.add(item);
            totalPrice = totalPrice.add(service.getPrice());
        }

        // 2. Update with items and total price, then save again
        request.setItems(items);
        request.setTotalPrice(totalPrice);

        request = requestRepository.save(request);

        return mapToResponse(request);
    }

    @Override
    public MedicalServiceRequestResponse getRequestById(Long id) {
        MedicalServiceRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical Service Request not found with id: " + id));
        return mapToResponse(request);
    }

    @Override
    public Page<MedicalServiceRequestResponse> getRequests(Long medRecordId, MedicalServiceRequestStatus status, Pageable pageable) {
        Page<MedicalServiceRequest> requests;
        if (medRecordId != null && status != null) {
            requests = requestRepository.findByMedRecord_MedicalRecordIdAndStatus(medRecordId, status, pageable);
        } else if (medRecordId != null) {
            requests = requestRepository.findByMedRecord_MedicalRecordId(medRecordId, pageable);
        } else if (status != null) {
            requests = requestRepository.findByStatus(status, pageable);
        } else {
            requests = requestRepository.findAll(pageable);
        }
        return requests.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public MedicalServiceRequestResponse updateStatus(Long id, UpdateMedicalServiceRequestStatusRequest requestDto) {
        MedicalServiceRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical Service Request not found with id: " + id));

        MedicalServiceRequestStatus currentStatus = request.getStatus();
        MedicalServiceRequestStatus newStatus = requestDto.getStatus();

        if (currentStatus == MedicalServiceRequestStatus.CANCELLED || currentStatus == MedicalServiceRequestStatus.COMPLETED) {
            throw new BusinessException("Cannot update status from " + currentStatus);
        }

        if (newStatus == MedicalServiceRequestStatus.CANCELLED) {
            request.setCancelledAt(LocalDateTime.now());
        }

        request.setStatus(newStatus);
        return mapToResponse(requestRepository.save(request));
    }

    @Override
    @Transactional
    public MedicalServiceResultResponse createResult(Long requestId, UpdateMedicalServiceResultRequest requestDto) {
        MedicalServiceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical Service Request not found with id: " + requestId));

        if (request.getStatus() != MedicalServiceRequestStatus.IN_PROGRESS && request.getStatus() != MedicalServiceRequestStatus.COMPLETED) {
             // For simplicity, allow creating result when IN_PROGRESS or update to COMPLETED later. Or simply allow it.
        }
        
        if (resultRepository.findByMedicalServiceRequest_MedServiceRequestId(requestId).isPresent()) {
            throw new BusinessException("Result already exists for this request");
        }

        MedicalServiceResult result = new MedicalServiceResult();
        result.setMedicalServiceRequest(request);
        result.setResultData(requestDto.getResultData());

        return mapToResultResponse(resultRepository.save(result));
    }

    @Override
    @Transactional
    public MedicalServiceResultResponse updateResult(Long resultId, UpdateMedicalServiceResultRequest requestDto) {
        MedicalServiceResult result = resultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical Service Result not found with id: " + resultId));

        result.setResultData(requestDto.getResultData());
        return mapToResultResponse(resultRepository.save(result));
    }

    @Override
    public MedicalServiceResultResponse getResultByRequestId(Long requestId) {
        MedicalServiceResult result = resultRepository.findByMedicalServiceRequest_MedServiceRequestId(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical Service Result not found for request id: " + requestId));
        return mapToResultResponse(result);
    }

    private MedicalServiceRequestResponse mapToResponse(MedicalServiceRequest request) {
        MedicalServiceRequestResponse response = new MedicalServiceRequestResponse();
        response.setMedServiceRequestId(request.getMedServiceRequestId());
        response.setMedRecordId(request.getMedRecord().getMedicalRecordId());
        response.setRequestCode(request.getRequestCode());
        response.setStatus(request.getStatus().name());
        response.setPaymentStatus(request.getPaymentStatus().name());
        response.setTotalPrice(request.getTotalPrice());
        response.setCurrency(request.getCurrency());
        response.setNote(request.getNote());
        response.setCreatedAt(request.getCreatedAt());
        response.setUpdatedAt(request.getUpdatedAt());
        response.setConfirmedAt(request.getConfirmedAt());
        response.setCancelledAt(request.getCancelledAt());
        response.setPaidAt(request.getPaidAt());

        if (request.getItems() != null) {
            List<MedicalServiceRequestItemResponse> itemResponses = request.getItems().stream().map(item -> {
                MedicalServiceRequestItemResponse itemResponse = new MedicalServiceRequestItemResponse();
                itemResponse.setMedServiceId(item.getMedicalService().getMedServiceId());
                itemResponse.setMedicalServiceName(item.getMedicalService().getMedicalServiceName());
                itemResponse.setSnapshotPrice(item.getSnapshotPrice());
                return itemResponse;
            }).collect(Collectors.toList());
            response.setItems(itemResponses);
        }
        return response;
    }

    private MedicalServiceResultResponse mapToResultResponse(MedicalServiceResult result) {
        MedicalServiceResultResponse response = new MedicalServiceResultResponse();
        response.setMedServiceResultId(result.getMedServiceResultId());
        response.setMedServiceRequestId(result.getMedicalServiceRequest().getMedServiceRequestId());
        response.setResultData(result.getResultData());
        response.setCreatedAt(result.getCreatedAt());
        return response;
    }
}
