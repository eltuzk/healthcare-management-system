package com.healthcare.backend.controller;

import com.healthcare.backend.dto.request.MedicalServiceRequest;
import com.healthcare.backend.dto.response.MedicalServiceResponse;
import com.healthcare.backend.service.MedicalServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medical-services")
@RequiredArgsConstructor
public class MedicalServiceController {

    private final MedicalServiceService medicalServiceService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST', 'TECHNICIAN')")
    public ResponseEntity<List<MedicalServiceResponse>> getAllMedicalServices() {
        return ResponseEntity.ok(medicalServiceService.getAllMedicalServices());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST', 'TECHNICIAN')")
    public ResponseEntity<MedicalServiceResponse> getMedicalServiceById(@PathVariable Long id) {
        return ResponseEntity.ok(medicalServiceService.getMedicalServiceById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MedicalServiceResponse> createMedicalService(
            @Valid @RequestBody MedicalServiceRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                medicalServiceService.createMedicalService(request)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MedicalServiceResponse> updateMedicalService(
            @PathVariable Long id,
            @Valid @RequestBody MedicalServiceRequest request
    ) {
        return ResponseEntity.ok(medicalServiceService.updateMedicalService(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MedicalServiceResponse> deactivateMedicalService(@PathVariable Long id) {
        return ResponseEntity.ok(medicalServiceService.deactivateMedicalService(id));
    }
}