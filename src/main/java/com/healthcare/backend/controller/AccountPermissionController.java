package com.healthcare.backend.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.healthcare.backend.dto.request.AccountPermissionRequest;
import com.healthcare.backend.dto.response.AccountPermissionResponse;
import com.healthcare.backend.service.AccountPermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/account-permissions")
@RequiredArgsConstructor
public class AccountPermissionController {

    private final AccountPermissionService accountPermissionService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AccountPermissionResponse> assign(@Valid @RequestBody AccountPermissionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountPermissionService.assign(request));
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> revoke(@Valid @RequestBody AccountPermissionRequest request) {
        accountPermissionService.revoke(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/account/{accountId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<AccountPermissionResponse>> getByAccountId(@PathVariable Long accountId) {
        return ResponseEntity.ok(accountPermissionService.getByAccountId(accountId));
    }
}
