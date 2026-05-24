package com.healthcare.backend.controller;

import com.healthcare.backend.dto.request.CreateAppointmentRequest;
import com.healthcare.backend.dto.request.CreateWalkInAppointmentRequest;
import com.healthcare.backend.dto.request.SepayWebhookRequest;
import com.healthcare.backend.dto.response.AppointmentResponse;
import com.healthcare.backend.entity.enums.AppointmentStatus;
import com.healthcare.backend.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final com.healthcare.backend.service.PaymentRecordService paymentRecordService;

    @PostMapping
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody CreateAppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.create(request));
    }

    @PostMapping("/walk-in")
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public ResponseEntity<AppointmentResponse> createWalkInPaidAppointment(
            @Valid @RequestBody CreateWalkInAppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.createWalkInPaidAppointment(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getAll(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long doctorScheduleId,
            @RequestParam(required = false) AppointmentStatus status) {
        return ResponseEntity.ok(appointmentService.getAll(patientId, doctorId, doctorScheduleId, status));
    }

    @PostMapping("/{id}/check-in")
    public ResponseEntity<AppointmentResponse> checkIn(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.checkIn(id));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<AppointmentResponse> start(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.start(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.cancel(id));
    }

    @PostMapping("/sepay/webhook")
    public ResponseEntity<Map<String, Object>> handleSepayWebhook(
            @RequestHeader(name = "X-Secret-Key", required = false) String secretKeyHeader,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @RequestBody SepayWebhookRequest request) {
        
        System.out.println("=== RECEIVED SEPAY WEBHOOK ===");
        System.out.println("Webhook ID: " + request.getId());
        System.out.println("Gateway: " + request.getGateway());
        System.out.println("Code field: '" + request.getCode() + "'");
        System.out.println("Content field: '" + request.getContent() + "'");
        System.out.println("Description field: '" + request.getDescription() + "'");
        System.out.println("Transfer Amount: " + request.getTransferAmount());
        System.out.println("Transfer Type: " + request.getTransferType());

        // Support both X-Secret-Key header and standard SePay Authorization header
        String secretKey = secretKeyHeader;
        if ((secretKey == null || secretKey.isBlank()) && authorizationHeader != null && !authorizationHeader.isBlank()) {
            if (authorizationHeader.startsWith("Apikey ")) {
                secretKey = authorizationHeader.substring(7).trim();
            } else {
                secretKey = authorizationHeader.trim();
            }
        }

        String code = null;
        
        // 1. Prioritize scanning the raw content first
        if (request.getContent() != null && !request.getContent().isBlank()) {
            String contentUpper = request.getContent().toUpperCase();
            java.util.regex.Matcher mr = java.util.regex.Pattern.compile("MR-\\d+").matcher(contentUpper);
            if (mr.find()) code = mr.group();
            
            if (code == null) {
                java.util.regex.Matcher pr = java.util.regex.Pattern.compile("PR-\\d+").matcher(contentUpper);
                if (pr.find()) code = pr.group();
            }
            if (code == null) {
                java.util.regex.Matcher apt = java.util.regex.Pattern.compile("APT-[A-Z0-9]+").matcher(contentUpper);
                if (apt.find()) code = apt.group();
            }
            if (code == null) {
                java.util.regex.Matcher dk = java.util.regex.Pattern.compile("DK\\d+").matcher(contentUpper);
                if (dk.find()) code = dk.group();
            }
        }
        
        // 2. Search in raw description (very common for banks to put it here)
        if (code == null && request.getDescription() != null && !request.getDescription().isBlank()) {
            String descUpper = request.getDescription().toUpperCase();
            java.util.regex.Matcher mr = java.util.regex.Pattern.compile("MR-\\d+").matcher(descUpper);
            if (mr.find()) code = mr.group();
            
            if (code == null) {
                java.util.regex.Matcher pr = java.util.regex.Pattern.compile("PR-\\d+").matcher(descUpper);
                if (pr.find()) code = pr.group();
            }
            if (code == null) {
                java.util.regex.Matcher apt = java.util.regex.Pattern.compile("APT-[A-Z0-9]+").matcher(descUpper);
                if (apt.find()) code = apt.group();
            }
            if (code == null) {
                java.util.regex.Matcher dk = java.util.regex.Pattern.compile("DK\\d+").matcher(descUpper);
                if (dk.find()) code = dk.group();
            }
        }
        
        // 3. Search in code field or fall back
        if (code == null && request.getCode() != null && !request.getCode().isBlank()) {
            String codeUpper = request.getCode().trim().toUpperCase();
            java.util.regex.Matcher mr = java.util.regex.Pattern.compile("MR-\\d+").matcher(codeUpper);
            if (mr.find()) {
                code = mr.group();
            } else {
                java.util.regex.Matcher pr = java.util.regex.Pattern.compile("PR-\\d+").matcher(codeUpper);
                if (pr.find()) {
                    code = pr.group();
                } else {
                    java.util.regex.Matcher apt = java.util.regex.Pattern.compile("APT-[A-Z0-9]+").matcher(codeUpper);
                    if (apt.find()) {
                        code = apt.group();
                    } else {
                        java.util.regex.Matcher dk = java.util.regex.Pattern.compile("DK\\d+").matcher(codeUpper);
                        if (dk.find()) {
                            code = dk.group();
                        } else {
                            code = codeUpper;
                        }
                    }
                }
            }
        }

        System.out.println("Resolved code for webhook: '" + code + "'");

        if (code != null && code.startsWith("MR-")) {
            com.healthcare.backend.dto.response.PaymentRecordResponse response =
                    paymentRecordService.confirmMedicalRecordPaymentFromSepayWebhook(request, secretKey);
            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                    "success", true,
                    "medicalRecordId", response.getMedicalRecordId() != null ? response.getMedicalRecordId() : 0L,
                    "requestCode", response.getRequestCode()));
        } else if (code != null && code.startsWith("PR-")) {
            com.healthcare.backend.dto.response.PaymentRecordResponse response =
                    paymentRecordService.confirmPrescriptionPaymentFromSepayWebhook(request, secretKey);
            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                    "success", true,
                    "prescriptionId", response.getPrescriptionId() != null ? response.getPrescriptionId() : 0L,
                    "requestCode", response.getRequestCode()));
        } else {
            AppointmentResponse response = appointmentService.confirmPaymentFromSepayWebhook(request, secretKey);
            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                    "success", true,
                    "appointmentId", response.getAppointmentId(),
                    "appointmentCode", response.getAppointmentCode()));
        }
    }
}
