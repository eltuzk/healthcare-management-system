package com.healthcare.backend.controller;

import com.healthcare.backend.dto.request.TechnicianRequest;
import com.healthcare.backend.dto.response.TechnicianResponse;
import com.healthcare.backend.security.UserPrincipal;
import com.healthcare.backend.service.TechnicianService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/technicians")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class TechnicianController {

    private final TechnicianService technicianService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Page<TechnicianResponse>> getAllTechnicians(Pageable pageable) {
        return ResponseEntity.ok(technicianService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<TechnicianResponse> getTechnicianById(@PathVariable Long id) {
        return ResponseEntity.ok(technicianService.getById(id));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_TECHNICIAN')")
    public ResponseEntity<TechnicianResponse> getMe(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(technicianService.getMe(userPrincipal.email()));
    }

    @PutMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_TECHNICIAN')")
    public ResponseEntity<TechnicianResponse> updateMe(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody TechnicianRequest request) {
        return ResponseEntity.ok(technicianService.updateMe(userPrincipal.email(), request));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<TechnicianResponse> createTechnician(@Valid @RequestBody TechnicianRequest request) {
        TechnicianResponse response = technicianService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<TechnicianResponse> updateTechnician(
            @PathVariable Long id, 
            @Valid @RequestBody TechnicianRequest request) {
        return ResponseEntity.ok(technicianService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteTechnician(@PathVariable Long id) {
        technicianService.delete(id);
        return ResponseEntity.noContent().build();
    }
}