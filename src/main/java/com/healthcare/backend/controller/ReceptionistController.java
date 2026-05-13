package com.healthcare.backend.controller;

import com.healthcare.backend.dto.request.ReceptionistRequest;
import com.healthcare.backend.dto.response.ReceptionistResponse;
import com.healthcare.backend.security.UserPrincipal;
import com.healthcare.backend.service.ReceptionistService;
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
@RequestMapping("/api/receptionists")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ReceptionistController {

    private final ReceptionistService receptionistService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Page<ReceptionistResponse>> getAllReceptionists(Pageable pageable) {
        return ResponseEntity.ok(receptionistService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ReceptionistResponse> getReceptionistById(@PathVariable Long id) {
        return ResponseEntity.ok(receptionistService.getById(id));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_RECEPTIONIST')")
    public ResponseEntity<ReceptionistResponse> getMe(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(receptionistService.getMe(userPrincipal.email()));
    }

    @PutMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_RECEPTIONIST')")
    public ResponseEntity<ReceptionistResponse> updateMe(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ReceptionistRequest request) {
        return ResponseEntity.ok(receptionistService.updateMe(userPrincipal.email(), request));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ReceptionistResponse> createReceptionist(@Valid @RequestBody ReceptionistRequest request) {
        ReceptionistResponse response = receptionistService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ReceptionistResponse> updateReceptionist(
            @PathVariable Long id, 
            @Valid @RequestBody ReceptionistRequest request) {
        return ResponseEntity.ok(receptionistService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteReceptionist(@PathVariable Long id) {
        receptionistService.delete(id);
        return ResponseEntity.noContent().build();
    }
}