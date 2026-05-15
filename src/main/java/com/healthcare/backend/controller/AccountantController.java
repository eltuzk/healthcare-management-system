package com.healthcare.backend.controller;

import com.healthcare.backend.dto.request.AccountantRequest;
import com.healthcare.backend.dto.response.AccountantResponse;
import com.healthcare.backend.security.UserPrincipal;
import com.healthcare.backend.service.AccountantService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accountants")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AccountantController {

    private final AccountantService accountantService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Page<AccountantResponse>> getAllAccountants(Pageable pageable) {
        return ResponseEntity.ok(accountantService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AccountantResponse> getAccountantById(@PathVariable Long id) {
        return ResponseEntity.ok(accountantService.getById(id));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_ACCOUNTANT')")
    public ResponseEntity<AccountantResponse> getMe(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(accountantService.getMe(userPrincipal.email()));
    }

    @PutMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_ACCOUNTANT')")
    public ResponseEntity<AccountantResponse> updateMe(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody AccountantRequest request) {
        return ResponseEntity.ok(accountantService.updateMe(userPrincipal.email(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AccountantResponse> updateAccountant(
            @PathVariable Long id, 
            @Valid @RequestBody AccountantRequest request) {
        return ResponseEntity.ok(accountantService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteAccountant(@PathVariable Long id) {
        accountantService.delete(id);
        return ResponseEntity.noContent().build();
    }
}