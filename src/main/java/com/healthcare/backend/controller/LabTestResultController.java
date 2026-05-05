package com.healthcare.backend.controller;

import com.healthcare.backend.dto.request.UpdateLabTestResultRequest;
import com.healthcare.backend.dto.response.ApiResponse;
import com.healthcare.backend.dto.response.LabTestResultResponse;
import com.healthcare.backend.service.LabTestRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lab-test-results")
@RequiredArgsConstructor
public class LabTestResultController {

    private final LabTestRequestService labTestRequestService;

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<ApiResponse<LabTestResultResponse>> updateResult(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLabTestResultRequest request) {
        return ResponseEntity.ok(ApiResponse.success(labTestRequestService.updateResult(id, request)));
    }
}
