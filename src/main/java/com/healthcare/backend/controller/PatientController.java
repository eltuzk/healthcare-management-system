package com.healthcare.backend.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthcare.backend.dto.request.PatientRequest;
import com.healthcare.backend.dto.response.PatientResponse;
import com.healthcare.backend.service.PatientService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/patients")
public class PatientController {
    @Autowired
    private PatientService patientService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<Page<PatientResponse>> getAllPatients(@ParameterObject Pageable pageable) {
        Page<PatientResponse> res = patientService.getAllPatients(pageable);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{patientId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RECEPTIONIST', 'DOCTOR') or (hasAuthority('PATIENT') and principal.patient = #patientId)")
    public ResponseEntity<PatientResponse> getPatientById(@PathVariable Long patientId) {
        PatientResponse res = patientService.getPatientById(patientId);
        return ResponseEntity.ok(res);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<PatientResponse> createPatient(@Valid @RequestBody PatientRequest patientRequest) {
        PatientResponse res = patientService.createPatient(patientRequest);
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{patientId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RECEPTIONIST') or (hasAuthority('PATIENT') and principal.patient == #patientId)")
    public ResponseEntity<PatientResponse> updatePatient(@PathVariable Long patientId, @Valid @RequestBody PatientRequest patientRequest) {
        PatientResponse res = patientService.updatePatient(patientId, patientRequest);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{patientId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> deletePatient(@PathVariable Long patientId) {
        patientService.deletePatient(patientId);
        return ResponseEntity.ok("Deleted successfully.");
    }

}
