package com.healthcare.backend.controller;

import com.healthcare.backend.dto.request.AdmissionRecordRequest;
import com.healthcare.backend.dto.request.AdmissionRequestRequest;
import com.healthcare.backend.dto.request.AdmissionStatusUpdateRequest;
import com.healthcare.backend.dto.response.AdmissionRecordResponse;
import com.healthcare.backend.dto.response.AdmissionRequestResponse;
import com.healthcare.backend.service.AdmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdmissionController {

    private final AdmissionService admissionService;

    // ─────────────────────────────────────────────────────────────
    // AdmissionRequest
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/admission-requests")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<List<AdmissionRequestResponse>> getAll() {
        return ResponseEntity.ok(admissionService.getAll());
    }

    @GetMapping("/admission-requests/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<AdmissionRequestResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(admissionService.getById(id));
    }

    @GetMapping("/patients/{patientId}/admissions")
    @PreAuthorize("hasAnyAuthority('ROLE_DOCTOR', 'ROLE_PATIENT')")
    public ResponseEntity<List<AdmissionRequestResponse>> getByPatientId(@PathVariable Long patientId) {
        return ResponseEntity.ok(admissionService.getByPatientId(patientId));
    }

    @PostMapping("/admission-requests")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public ResponseEntity<AdmissionRequestResponse> create(@Valid @RequestBody AdmissionRequestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(admissionService.create(request));
    }

    @PutMapping("/admission-requests/{id}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_DOCTOR', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<AdmissionRequestResponse> updateStatus(@PathVariable Long id,
            @Valid @RequestBody AdmissionStatusUpdateRequest request) {
        return ResponseEntity.ok(admissionService.updateStatus(id, request));
    }

    // ─────────────────────────────────────────────────────────────
    // AdmissionRecord
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/admission-requests/{admissionId}/records")
    @PreAuthorize("hasAnyAuthority('ROLE_DOCTOR', 'ROLE_NURSE')")
    public ResponseEntity<List<AdmissionRecordResponse>> getRecords(@PathVariable Long admissionId) {
        return ResponseEntity.ok(admissionService.getRecords(admissionId));
    }

    @PostMapping("/admission-requests/{admissionId}/records")
    @PreAuthorize("hasAnyAuthority('ROLE_DOCTOR', 'ROLE_NURSE')")
    public ResponseEntity<AdmissionRecordResponse> createRecord(@PathVariable Long admissionId,
            @Valid @RequestBody AdmissionRecordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(admissionService.createRecord(admissionId, request));
    }

    @PutMapping("/admission-records/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_DOCTOR', 'ROLE_NURSE')")
    public ResponseEntity<AdmissionRecordResponse> updateRecord(@PathVariable Long id,
            @Valid @RequestBody AdmissionRecordRequest request) {
        return ResponseEntity.ok(admissionService.updateRecord(id, request));
    }
}