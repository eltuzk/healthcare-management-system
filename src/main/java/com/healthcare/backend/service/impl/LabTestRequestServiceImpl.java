package com.healthcare.backend.service.impl;

import com.healthcare.backend.dto.request.CreateLabTestRequestRequest;
import com.healthcare.backend.dto.request.CreateLabTestResultRequest;
import com.healthcare.backend.dto.request.UpdateLabTestRequestStatusRequest;
import com.healthcare.backend.dto.request.UpdateLabTestResultRequest;
import com.healthcare.backend.dto.response.LabTestRequestResponse;
import com.healthcare.backend.dto.response.LabTestResultResponse;
import com.healthcare.backend.entity.*;
import com.healthcare.backend.entity.enums.LabTestRequestStatus;
import com.healthcare.backend.entity.enums.PaymentStatus;
import com.healthcare.backend.exception.BusinessException;
import com.healthcare.backend.exception.DuplicateResourceException;
import com.healthcare.backend.exception.ResourceNotFoundException;
import com.healthcare.backend.mapper.LabTestRequestMapper;
import com.healthcare.backend.mapper.LabTestResultMapper;
import com.healthcare.backend.repository.LabTestRepository;
import com.healthcare.backend.repository.LabTestRequestRepository;
import com.healthcare.backend.repository.LabTestResultRepository;
import com.healthcare.backend.repository.MedicalRecordRepository;
import com.healthcare.backend.service.LabTestRequestService;
import com.healthcare.backend.service.MedicalRecordBillingService;
import com.healthcare.backend.service.MedicalRecordWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LabTestRequestServiceImpl implements LabTestRequestService {

    private final LabTestRequestRepository labTestRequestRepository;
    private final LabTestResultRepository labTestResultRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final LabTestRepository labTestRepository;
    private final LabTestRequestMapper labTestRequestMapper;
    private final LabTestResultMapper labTestResultMapper;
    private final MedicalRecordBillingService medicalRecordBillingService;
    private final MedicalRecordWorkflowService medicalRecordWorkflowService;

    @Override
    @Transactional
    public LabTestRequestResponse createRequest(CreateLabTestRequestRequest request) {
        if (request.getLabTestIds() == null || request.getLabTestIds().isEmpty()) {
            throw new BusinessException("At least one Lab Test must be selected");
        }

        MedicalRecord medRecord = medicalRecordRepository.findByIdForUpdate(request.getMedRecordId())
                .orElseThrow(() -> new ResourceNotFoundException("Medical Record not found with id: " + request.getMedRecordId()));
        medicalRecordWorkflowService.validateCanCreateRequest(medRecord);

        LabTestRequest labTestRequest = new LabTestRequest();
        labTestRequest.setMedRecord(medRecord);
        labTestRequest.setRequestCode("LTR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        labTestRequest.setNote(request.getNote());
        
        List<LabTestRequestItem> items = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (Long labTestId : request.getLabTestIds()) {
            LabTest labTest = labTestRepository.findById(labTestId)
                    .orElseThrow(() -> new ResourceNotFoundException("Lab Test not found with id: " + labTestId));

            BigDecimal price = labTest.getPrice() != null ? labTest.getPrice() : BigDecimal.ZERO;
            LabTestRequestItem item = new LabTestRequestItem(labTestRequest, labTest, price);
            items.add(item);
            totalPrice = totalPrice.add(price);
        }

        labTestRequest.setItems(items);
        labTestRequest.setTotalPrice(totalPrice);
        labTestRequest.setStatus(LabTestRequestStatus.NOT_COLLECTED);
        labTestRequest.setPaymentStatus(PaymentStatus.UNPAID);

        LabTestRequest savedRequest = labTestRequestRepository.save(labTestRequest);
        medicalRecordBillingService.syncBilling(medRecord.getMedicalRecordId());

        return labTestRequestMapper.toResponse(savedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public LabTestRequestResponse getRequestById(Long id) {
        LabTestRequest request = labTestRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab Test Request not found with id: " + id));
        return labTestRequestMapper.toResponse(request);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LabTestRequestResponse> getRequests(Long medRecordId, LabTestRequestStatus status, Pageable pageable) {
        Page<LabTestRequest> requests;
        if (medRecordId != null && status != null) {
            requests = labTestRequestRepository.findByMedRecord_MedicalRecordIdAndStatus(medRecordId, status, pageable);
        } else if (medRecordId != null) {
            requests = labTestRequestRepository.findByMedRecord_MedicalRecordId(medRecordId, pageable);
        } else if (status != null) {
            requests = labTestRequestRepository.findByStatus(status, pageable);
        } else {
            requests = labTestRequestRepository.findAll(pageable);
        }
        return requests.map(labTestRequestMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabTestRequestResponse> getRequestsByMedRecord(Long medRecordId) {
        return labTestRequestRepository.findByMedRecord_MedicalRecordId(medRecordId).stream()
                .map(labTestRequestMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LabTestRequestResponse updateStatus(Long id, UpdateLabTestRequestStatusRequest request) {
        LabTestRequest labTestRequest = labTestRequestRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab Test Request not found with id: " + id));
        medicalRecordRepository.findByIdForUpdate(labTestRequest.getMedRecord().getMedicalRecordId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Medical Record not found with id: " + labTestRequest.getMedRecord().getMedicalRecordId()
                ));

        LabTestRequestStatus currentStatus = labTestRequest.getStatus();
        LabTestRequestStatus newStatus = request.getStatus();

        medicalRecordWorkflowService.validateCanUpdateRequest(labTestRequest.getMedRecord());

        if (currentStatus == LabTestRequestStatus.RESULT_AVAILABLE) {
            throw new BusinessException("Cannot update status of a " + currentStatus + " request");
        }

        if (newStatus != LabTestRequestStatus.SAMPLE_COLLECTED) {
            throw new BusinessException("Lab test request status can only be updated to SAMPLE_COLLECTED manually");
        }

        labTestRequest.setStatus(newStatus);
        LabTestRequest savedRequest = labTestRequestRepository.save(labTestRequest);
        medicalRecordBillingService.syncBilling(savedRequest.getMedRecord().getMedicalRecordId());
        return labTestRequestMapper.toResponse(savedRequest);
    }

    @Override
    @Transactional
    public LabTestResultResponse createResult(Long requestId, CreateLabTestResultRequest request) {
        LabTestRequest labTestRequest = labTestRequestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab Test Request not found with id: " + requestId));
        medicalRecordRepository.findByIdForUpdate(labTestRequest.getMedRecord().getMedicalRecordId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Medical Record not found with id: " + labTestRequest.getMedRecord().getMedicalRecordId()
                ));

        medicalRecordWorkflowService.validateCanUpdateRequest(labTestRequest.getMedRecord());

        if (labTestResultRepository.findByLabTestRequest_LabTestRequestId(requestId).isPresent()) {
            throw new DuplicateResourceException("Result already exists for this request");
        }

        if (labTestRequest.getStatus() != LabTestRequestStatus.SAMPLE_COLLECTED) {
            throw new BusinessException("Sample must be collected before adding lab test result");
        }

        LabTestResult result = new LabTestResult();
        result.setLabTestRequest(labTestRequest);
        result.setResultData(request.getResultData());
        
        labTestRequest.setStatus(LabTestRequestStatus.RESULT_AVAILABLE);
        labTestRequestRepository.save(labTestRequest);
        LabTestResult savedResult = labTestResultRepository.save(result);
        medicalRecordWorkflowService.completeIfReady(labTestRequest.getMedRecord().getMedicalRecordId());

        return labTestResultMapper.toResponse(savedResult);
    }

    @Override
    @Transactional
    public LabTestResultResponse updateResult(Long resultId, UpdateLabTestResultRequest request) {
        LabTestResult result = labTestResultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab Test Result not found with id: " + resultId));
        medicalRecordWorkflowService.validateCanUpdateRequest(result.getLabTestRequest().getMedRecord());

        result.setResultData(request.getResultData());
        return labTestResultMapper.toResponse(labTestResultRepository.save(result));
    }

    @Override
    @Transactional(readOnly = true)
    public LabTestResultResponse getResultByRequestId(Long requestId) {
        LabTestResult result = labTestResultRepository.findByLabTestRequest_LabTestRequestId(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab Test Result not found for request id: " + requestId));
        return labTestResultMapper.toResponse(result);
    }
}
