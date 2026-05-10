package com.healthcare.backend.controller;

import com.healthcare.backend.dto.request.CreateMedicalServiceRequestRequest;
import com.healthcare.backend.dto.request.UpdateMedicalServiceRequestStatusRequest;
import com.healthcare.backend.dto.request.UpdateMedicalServiceResultRequest;
import com.healthcare.backend.dto.response.ApiResponse;
import com.healthcare.backend.dto.response.MedicalServiceRequestResponse;
import com.healthcare.backend.dto.response.MedicalServiceResultResponse;
import com.healthcare.backend.entity.enums.MedicalServiceRequestStatus;
import com.healthcare.backend.service.MedicalServiceRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medical-service-requests")
@RequiredArgsConstructor
public class MedicalServiceRequestController {

    private final MedicalServiceRequestService service;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<MedicalServiceRequestResponse>> createRequest(
            @Valid @RequestBody CreateMedicalServiceRequestRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.createRequest(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<Page<MedicalServiceRequestResponse>>> getRequests(
            @RequestParam(required = false) Long medRecordId,
            @RequestParam(required = false) MedicalServiceRequestStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(service.getRequests(medRecordId, status, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<MedicalServiceRequestResponse>> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getRequestById(id)));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<MedicalServiceRequestResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMedicalServiceRequestStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.updateStatus(id, request)));
    }

    @PostMapping("/{requestId}/results")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<MedicalServiceResultResponse>> createResult(
            @PathVariable Long requestId,
            @Valid @RequestBody UpdateMedicalServiceResultRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.createResult(requestId, request)));
    }

    @GetMapping("/{requestId}/results")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<ApiResponse<MedicalServiceResultResponse>> getResults(@PathVariable Long requestId) {
        return ResponseEntity.ok(ApiResponse.success(service.getResultByRequestId(requestId)));
    }
}
