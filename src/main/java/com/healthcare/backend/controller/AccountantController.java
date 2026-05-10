package com.healthcare.backend.controller;

import com.healthcare.backend.dto.request.AccountantRequest;
import com.healthcare.backend.dto.response.AccountantResponse;
import com.healthcare.backend.service.AccountantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accountants")
@RequiredArgsConstructor
public class AccountantController {

    private final AccountantService accountantService;

    @GetMapping
    public ResponseEntity<Page<AccountantResponse>> getAllAccountants(Pageable pageable) {
        return ResponseEntity.ok(accountantService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountantResponse> getAccountantById(@PathVariable Long id) {
        return ResponseEntity.ok(accountantService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountantResponse> updateAccountant(
            @PathVariable Long id, 
            @Valid @RequestBody AccountantRequest request) {
        return ResponseEntity.ok(accountantService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccountant(@PathVariable Long id) {
        accountantService.delete(id);
        return ResponseEntity.noContent().build();
    }
}