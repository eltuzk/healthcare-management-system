package com.healthcare.backend.controller;

import java.util.Map;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthcare.backend.dto.request.AccountRequestDTO;
import com.healthcare.backend.dto.request.ChangePasswordRequestDTO;
import com.healthcare.backend.dto.response.AccountResponseDTO;
import com.healthcare.backend.service.AccountServiceInterface;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    @Autowired
    private AccountServiceInterface accountService;

    @GetMapping
    public ResponseEntity<Page<AccountResponseDTO>> getAllAccounts(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(accountService.getAllAccounts(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponseDTO> getAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @PostMapping
    public ResponseEntity<AccountResponseDTO> createAccount(@RequestBody AccountRequestDTO accountRequestDTO) {
        return ResponseEntity.ok(accountService.createAccount(accountRequestDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountResponseDTO> updateAccount(@PathVariable Long id, @RequestBody AccountRequestDTO accountRequestDTO) {
        return ResponseEntity.ok(accountService.updateAccount(id, accountRequestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.ok("Account deleted successfully.");
    }

    @PostMapping("/{accountId}/permissions/{permissionId}")
    public ResponseEntity<String> addPermissionToAccount(@PathVariable Long accountId, @PathVariable Long permissionId) {
        accountService.addPermissionToAccount(accountId, permissionId);
        return ResponseEntity.ok("Permission added successfully.");
    }

    @GetMapping("/{accountId}/permissions")
    public ResponseEntity<Map<String, Object>> getPermissionsByAccount(@PathVariable Long accountId, @ParameterObject Pageable pageable) {
        try {
            Map<String, Object> res = accountService.getPermissionsByAccount(pageable, accountId);
            return ResponseEntity.ok(res);
        }
        catch(Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
        @AuthenticationPrincipal String email,
        @Valid @RequestBody ChangePasswordRequestDTO changePasswordRequest
    ) {
        accountService.changePassword(email, changePasswordRequest);
        return ResponseEntity.ok("Password changed successfully.");
    }
}
