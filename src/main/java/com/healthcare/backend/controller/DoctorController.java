package com.healthcare.backend.controller;

import com.healthcare.backend.dto.request.DoctorRequest;
import com.healthcare.backend.dto.response.DoctorResponse;
import com.healthcare.backend.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/doctors")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<Page<DoctorResponse>> getAll(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(doctorService.getAll(pageable));
    }

    @GetMapping("/{doctorId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<DoctorResponse> getById(@PathVariable Long doctorId) {
        return ResponseEntity.ok(doctorService.getById(doctorId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<DoctorResponse> create(@Valid @RequestBody DoctorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorService.create(request));
    }

    @PutMapping("/{doctorId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<DoctorResponse> update(
            @PathVariable Long doctorId,
            @Valid @RequestBody DoctorRequest request
    ) {
        return ResponseEntity.ok(doctorService.update(doctorId, request));
    }

    @DeleteMapping("/{doctorId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long doctorId) {
        doctorService.delete(doctorId);
        return ResponseEntity.noContent().build();
    }
}
