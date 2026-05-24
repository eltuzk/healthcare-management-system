package com.healthcare.backend.controller;

import com.healthcare.backend.dto.request.PrescriptionRequest;
import com.healthcare.backend.dto.response.PrescriptionResponse;
import com.healthcare.backend.service.PrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @GetMapping
    public ResponseEntity<List<PrescriptionResponse>> getAllPrescriptions() {
        return ResponseEntity.ok(prescriptionService.getAllPrescriptions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrescriptionResponse> getPrescriptionById(@PathVariable Long id) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionById(id));
    }

    @GetMapping("/medical-record/{medicalRecordId}")
    public ResponseEntity<PrescriptionResponse> getPrescriptionByMedicalRecordId(
            @PathVariable Long medicalRecordId
    ) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionByMedicalRecordId(medicalRecordId));
    }

    @PostMapping
    public ResponseEntity<PrescriptionResponse> createPrescription(
            @Valid @RequestBody PrescriptionRequest request
    ) {
        PrescriptionResponse response = prescriptionService.createPrescription(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PrescriptionResponse> updatePrescription(
            @PathVariable Long id,
            @Valid @RequestBody PrescriptionRequest request
    ) {
        return ResponseEntity.ok(prescriptionService.updatePrescription(id, request));
    }

    @PostMapping("/{id}/dispense")
    public ResponseEntity<PrescriptionResponse> dispensePrescription(@PathVariable Long id) {
        return ResponseEntity.ok(prescriptionService.dispensePrescription(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PrescriptionResponse> deactivatePrescription(@PathVariable Long id) {
        return ResponseEntity.ok(prescriptionService.deactivatePrescription(id));
    }
}