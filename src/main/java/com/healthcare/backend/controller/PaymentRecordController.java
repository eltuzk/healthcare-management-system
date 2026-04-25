package com.healthcare.backend.controller;

import com.healthcare.backend.dto.response.PaymentRecordResponse;
import com.healthcare.backend.entity.enums.PaymentStatus;
import com.healthcare.backend.service.PaymentRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/payment-records")
@RequiredArgsConstructor
public class PaymentRecordController {

    private final PaymentRecordService paymentRecordService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ACCOUNTANT', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<List<PaymentRecordResponse>> getAll(
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) Long appointmentId,
            @RequestParam(required = false) Long medicalRecordId
    ) {
        return ResponseEntity.ok(paymentRecordService.getAll(paymentStatus, appointmentId, medicalRecordId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ACCOUNTANT', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<PaymentRecordResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentRecordService.getById(id));
    }
}
