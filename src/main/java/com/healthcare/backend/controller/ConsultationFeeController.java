package com.healthcare.backend.controller;

import com.healthcare.backend.dto.request.ConsultationFeeRequest;
import com.healthcare.backend.dto.response.ConsultationFeeResponse;
import com.healthcare.backend.service.ConsultationFeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/consultation-fees")
@RequiredArgsConstructor
public class ConsultationFeeController {

    private final ConsultationFeeService consultationFeeService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ConsultationFeeResponse> create(@Valid @RequestBody ConsultationFeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(consultationFeeService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPTIONIST', 'ROLE_DOCTOR')")
    public ResponseEntity<List<ConsultationFeeResponse>> getAll() {
        return ResponseEntity.ok(consultationFeeService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPTIONIST', 'ROLE_DOCTOR')")
    public ResponseEntity<ConsultationFeeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(consultationFeeService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ConsultationFeeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ConsultationFeeRequest request
    ) {
        return ResponseEntity.ok(consultationFeeService.update(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ConsultationFeeResponse> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(consultationFeeService.deactivate(id));
    }
}
