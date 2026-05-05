package com.healthcare.backend.controller;

import com.healthcare.backend.dto.request.LabTestRequest;
import com.healthcare.backend.dto.response.LabTestResponse;
import com.healthcare.backend.service.LabTestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lab-tests")
@RequiredArgsConstructor
public class LabTestController {

    private final LabTestService labTestService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST', 'TECHNICIAN')")
    public ResponseEntity<List<LabTestResponse>> getAllLabTests() {
        return ResponseEntity.ok(labTestService.getAllLabTests());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST', 'TECHNICIAN')")
    public ResponseEntity<LabTestResponse> getLabTestById(@PathVariable Long id) {
        return ResponseEntity.ok(labTestService.getLabTestById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LabTestResponse> createLabTest(@Valid @RequestBody LabTestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(labTestService.createLabTest(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LabTestResponse> updateLabTest(
            @PathVariable Long id,
            @Valid @RequestBody LabTestRequest request
    ) {
        return ResponseEntity.ok(labTestService.updateLabTest(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LabTestResponse> deactivateLabTest(@PathVariable Long id) {
        return ResponseEntity.ok(labTestService.deactivateLabTest(id));
    }
}