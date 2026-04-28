package com.healthcare.backend.controller;

import com.healthcare.backend.dto.request.CreateMedicalRecordRequest;
import com.healthcare.backend.dto.request.UpdateMedicalRecordRequest;
import com.healthcare.backend.dto.response.MedicalRecordResponse;
import com.healthcare.backend.entity.enums.MedicalRecordStatus;
import com.healthcare.backend.service.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @PostMapping("/from-appointment/{appointmentId}")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public ResponseEntity<MedicalRecordResponse> createFromAppointment(
            @PathVariable Long appointmentId,
            @Valid @RequestBody CreateMedicalRecordRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(medicalRecordService.createFromAppointment(appointmentId, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_DOCTOR', 'ROLE_ADMIN', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<MedicalRecordResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(medicalRecordService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_DOCTOR', 'ROLE_ADMIN', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<List<MedicalRecordResponse>> getAll(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) MedicalRecordStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(medicalRecordService.getAll(patientId, doctorId, status, date));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public ResponseEntity<MedicalRecordResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMedicalRecordRequest request
    ) {
        return ResponseEntity.ok(medicalRecordService.update(id, request));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public ResponseEntity<MedicalRecordResponse> complete(@PathVariable Long id) {
        return ResponseEntity.ok(medicalRecordService.complete(id));
    }

    @PostMapping("/{id}/lock")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public ResponseEntity<MedicalRecordResponse> lock(@PathVariable Long id) {
        return ResponseEntity.ok(medicalRecordService.lock(id));
    }
}
