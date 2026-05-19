package com.healthcare.backend.controller;

import com.healthcare.backend.dto.request.AdministratorRequest;
import com.healthcare.backend.dto.response.AdministratorResponse;
import com.healthcare.backend.service.AdministratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.healthcare.backend.security.UserPrincipal;

@RestController
@RequestMapping("/api/administrators")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AdministratorController {

    private final AdministratorService administratorService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Page<AdministratorResponse>> getAll(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(administratorService.getAll(pageable));
    }

    @GetMapping("/{adminId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AdministratorResponse> getById(@PathVariable Long adminId) {
        return ResponseEntity.ok(administratorService.getById(adminId));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AdministratorResponse> getMe(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(administratorService.getMe(userPrincipal.email()));
    }

    @PutMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AdministratorResponse> updateMe(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody AdministratorRequest request) {
        return ResponseEntity.ok(administratorService.updateMe(userPrincipal.email(), request));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AdministratorResponse> create(@Valid @RequestBody AdministratorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(administratorService.create(request));
    }

    @PutMapping("/{adminId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AdministratorResponse> update(
            @PathVariable Long adminId,
            @Valid @RequestBody AdministratorRequest request) {
        return ResponseEntity.ok(administratorService.update(adminId, request));
    }

    @DeleteMapping("/{adminId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long adminId) {
        administratorService.delete(adminId);
        return ResponseEntity.noContent().build();
    }
}
