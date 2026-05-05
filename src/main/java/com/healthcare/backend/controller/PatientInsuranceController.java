package com.healthcare.backend.controller;

import com.healthcare.backend.dto.request.PatientInsuranceRequest;
import com.healthcare.backend.dto.response.PatientInsuranceResponse;
import com.healthcare.backend.service.PatientInsuranceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patient-insurances")
@RequiredArgsConstructor
public class PatientInsuranceController {

    private final PatientInsuranceService patientInsuranceService;

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPTIONIST', 'ROLE_ACCOUNTANT')")
    public ResponseEntity<List<PatientInsuranceResponse>> getByPatientId(@PathVariable Long patientId) {
        return ResponseEntity.ok(patientInsuranceService.getByPatientId(patientId));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<PatientInsuranceResponse> create(@Valid @RequestBody PatientInsuranceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientInsuranceService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<PatientInsuranceResponse> update(@PathVariable Long id,
                                                           @Valid @RequestBody PatientInsuranceRequest request) {
        return ResponseEntity.ok(patientInsuranceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        patientInsuranceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
