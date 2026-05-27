package com.healthcare.backend.controller;

import com.healthcare.backend.dto.request.RecordMedicalRecordPaymentRequest;
import com.healthcare.backend.dto.response.PaymentRecordResponse;
import com.healthcare.backend.entity.enums.PaymentStatus;
import com.healthcare.backend.service.PaymentRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment-records")
@RequiredArgsConstructor
public class PaymentRecordController {

    private final PaymentRecordService paymentRecordService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ACCOUNTANT', 'ROLE_RECEPTIONIST', 'ROLE_PATIENT')")
    public ResponseEntity<List<PaymentRecordResponse>> getAll(
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) Long appointmentId,
            @RequestParam(required = false) Long medicalRecordId) {
        return ResponseEntity.ok(paymentRecordService.getAll(paymentStatus, appointmentId, medicalRecordId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ACCOUNTANT', 'ROLE_RECEPTIONIST', 'ROLE_PATIENT')")
    public ResponseEntity<PaymentRecordResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentRecordService.getById(id));
    }

    @PostMapping("/medical-records/{medicalRecordId}/cash")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ACCOUNTANT', 'ROLE_RECEPTIONIST')")
    public ResponseEntity<PaymentRecordResponse> recordMedicalRecordCashPayment(
            @PathVariable Long medicalRecordId,
            @Valid @RequestBody RecordMedicalRecordPaymentRequest request) {
        return ResponseEntity.ok(paymentRecordService.recordMedicalRecordCashPayment(medicalRecordId, request));
    }

    @PostMapping("/prescriptions/{prescriptionId}/cash")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ACCOUNTANT', 'ROLE_PHARMACIST')")
    public ResponseEntity<PaymentRecordResponse> recordPrescriptionCashPayment(
            @PathVariable Long prescriptionId) {
        return ResponseEntity.ok(paymentRecordService.recordPrescriptionCashPayment(prescriptionId));
    }

    @PostMapping("/medical-records/sepay/webhook")
    public ResponseEntity<Map<String, Object>> handleMedicalRecordSepayWebhook(
            @RequestHeader(name = "X-Secret-Key", required = false) String secretKeyHeader,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @RequestBody com.healthcare.backend.dto.request.SepayWebhookRequest request) {
        
        System.out.println("=== RECEIVED MEDICAL RECORD SEPAY WEBHOOK ===");
        System.out.println("Webhook ID: " + request.getId());
        System.out.println("Code: " + request.getCode());
        System.out.println("Content: " + request.getContent());
        System.out.println("Description: " + request.getDescription());
        System.out.println("Amount: " + request.getTransferAmount());

        String secretKey = secretKeyHeader;
        if ((secretKey == null || secretKey.isBlank()) && authorizationHeader != null && !authorizationHeader.isBlank()) {
            if (authorizationHeader.startsWith("Apikey ")) {
                secretKey = authorizationHeader.substring(7).trim();
            } else {
                secretKey = authorizationHeader.trim();
            }
        }

        PaymentRecordResponse response = paymentRecordService.confirmMedicalRecordPaymentFromSepayWebhook(request, secretKey);
        return ResponseEntity.status(org.springframework.http.HttpStatus.OK).body(Map.of(
                "success", true,
                "medicalRecordId", response.getMedicalRecordId() != null ? response.getMedicalRecordId() : 0L,
                "requestCode", response.getRequestCode()));
    }

    @PostMapping("/prescriptions/sepay/webhook")
    public ResponseEntity<Map<String, Object>> handlePrescriptionSepayWebhook(
            @RequestHeader(name = "X-Secret-Key", required = false) String secretKeyHeader,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @RequestBody com.healthcare.backend.dto.request.SepayWebhookRequest request) {
        
        System.out.println("=== RECEIVED PRESCRIPTION SEPAY WEBHOOK ===");
        System.out.println("Webhook ID: " + request.getId());
        System.out.println("Code: " + request.getCode());
        System.out.println("Content: " + request.getContent());
        System.out.println("Description: " + request.getDescription());
        System.out.println("Amount: " + request.getTransferAmount());

        String secretKey = secretKeyHeader;
        if ((secretKey == null || secretKey.isBlank()) && authorizationHeader != null && !authorizationHeader.isBlank()) {
            if (authorizationHeader.startsWith("Apikey ")) {
                secretKey = authorizationHeader.substring(7).trim();
            } else {
                secretKey = authorizationHeader.trim();
            }
        }

        PaymentRecordResponse response = paymentRecordService.confirmPrescriptionPaymentFromSepayWebhook(request, secretKey);
        return ResponseEntity.status(org.springframework.http.HttpStatus.OK).body(Map.of(
                "success", true,
                "prescriptionId", response.getPrescriptionId() != null ? response.getPrescriptionId() : 0L,
                "requestCode", response.getRequestCode()));
    }
}
