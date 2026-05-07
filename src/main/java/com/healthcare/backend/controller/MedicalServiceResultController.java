package com.healthcare.backend.controller;

import com.healthcare.backend.dto.request.UpdateMedicalServiceResultRequest;
import com.healthcare.backend.dto.response.ApiResponse;
import com.healthcare.backend.dto.response.MedicalServiceResultResponse;
import com.healthcare.backend.service.MedicalServiceRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/medical-service-results")
@RequiredArgsConstructor
public class MedicalServiceResultController {

    private final MedicalServiceRequestService service;

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<MedicalServiceResultResponse>> updateResult(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMedicalServiceResultRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.updateResult(id, request)));
    }
}
