package com.healthcare.backend.controller;

import com.healthcare.backend.dto.request.PharmacistRequest;
import com.healthcare.backend.dto.response.PharmacistResponse;
import com.healthcare.backend.security.UserPrincipal;
import com.healthcare.backend.service.PharmacistService;
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
@RequestMapping("/api/pharmacists")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PharmacistController {

    private final PharmacistService pharmacistService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Page<PharmacistResponse>> getAllPharmacists(Pageable pageable) {
        return ResponseEntity.ok(pharmacistService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<PharmacistResponse> getPharmacistById(@PathVariable Long id) {
        return ResponseEntity.ok(pharmacistService.getById(id));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_PHARMACIST')")
    public ResponseEntity<PharmacistResponse> getMe(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(pharmacistService.getMe(userPrincipal.email()));
    }

    @PutMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_PHARMACIST')")
    public ResponseEntity<PharmacistResponse> updateMe(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody PharmacistRequest request) {
        return ResponseEntity.ok(pharmacistService.updateMe(userPrincipal.email(), request));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<PharmacistResponse> createPharmacist(@Valid @RequestBody PharmacistRequest request) {
        PharmacistResponse response = pharmacistService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<PharmacistResponse> updatePharmacist(
            @PathVariable Long id, 
            @Valid @RequestBody PharmacistRequest request) {
        return ResponseEntity.ok(pharmacistService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deletePharmacist(@PathVariable Long id) {
        pharmacistService.delete(id);
        return ResponseEntity.noContent().build();
    }
}