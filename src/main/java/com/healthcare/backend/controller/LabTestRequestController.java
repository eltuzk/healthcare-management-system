package com.healthcare.backend.controller;

import com.healthcare.backend.dto.request.CreateLabTestRequestRequest;
import com.healthcare.backend.dto.request.CreateLabTestResultRequest;
import com.healthcare.backend.dto.request.UpdateLabTestRequestStatusRequest;
import com.healthcare.backend.dto.response.ApiResponse;
import com.healthcare.backend.dto.response.LabTestRequestResponse;
import com.healthcare.backend.dto.response.LabTestResultResponse;
import com.healthcare.backend.entity.enums.LabTestRequestStatus;
import com.healthcare.backend.service.LabTestRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lab-test-requests")
@RequiredArgsConstructor
public class LabTestRequestController {

    private final LabTestRequestService labTestRequestService;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<LabTestRequestResponse>> createRequest(
            @Valid @RequestBody CreateLabTestRequestRequest request) {
        return ResponseEntity.ok(ApiResponse.success(labTestRequestService.createRequest(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<Page<LabTestRequestResponse>>> getRequests(
            @RequestParam(required = false) Long medRecordId,
            @RequestParam(required = false) LabTestRequestStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(labTestRequestService.getRequests(medRecordId, status, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'TECHNICIAN', 'PATIENT')")
    public ResponseEntity<ApiResponse<LabTestRequestResponse>> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(labTestRequestService.getRequestById(id)));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")
    public ResponseEntity<ApiResponse<LabTestRequestResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLabTestRequestStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(labTestRequestService.updateStatus(id, request)));
    }

    @GetMapping("/medical-record/{medRecordId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<List<LabTestRequestResponse>>> getRequestsByMedRecord(
            @PathVariable Long medRecordId) {
        return ResponseEntity.ok(ApiResponse.success(labTestRequestService.getRequestsByMedRecord(medRecordId)));
    }

    @PostMapping("/{requestId}/results")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<ApiResponse<LabTestResultResponse>> createResult(
            @PathVariable Long requestId,
            @Valid @RequestBody CreateLabTestResultRequest request) {
        return ResponseEntity.ok(ApiResponse.success(labTestRequestService.createResult(requestId, request)));
    }

    @GetMapping("/{requestId}/results")
    @PreAuthorize("hasAnyRole('DOCTOR', 'TECHNICIAN', 'PATIENT')")
    public ResponseEntity<ApiResponse<LabTestResultResponse>> getResults(@PathVariable Long requestId) {
        return ResponseEntity.ok(ApiResponse.success(labTestRequestService.getResultByRequestId(requestId)));
    }
}
