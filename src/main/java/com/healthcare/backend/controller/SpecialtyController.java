package com.healthcare.backend.controller;

import com.healthcare.backend.dto.request.SpecialtyRequest;
import com.healthcare.backend.dto.response.SpecialtyResponse;
import com.healthcare.backend.service.SpecialtyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/specialties")
@RequiredArgsConstructor
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<SpecialtyResponse> create(@Valid @RequestBody SpecialtyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(specialtyService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPTIONIST', 'ROLE_DOCTOR')")
    public ResponseEntity<List<SpecialtyResponse>> getAll() {
        return ResponseEntity.ok(specialtyService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPTIONIST', 'ROLE_DOCTOR')")
    public ResponseEntity<SpecialtyResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(specialtyService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<SpecialtyResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody SpecialtyRequest request
    ) {
        return ResponseEntity.ok(specialtyService.update(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<SpecialtyResponse> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(specialtyService.deactivate(id));
    }
}
