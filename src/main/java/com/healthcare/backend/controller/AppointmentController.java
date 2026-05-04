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

    @PostMapping
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody CreateAppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.create(request));
    }

    @PostMapping("/walk-in")
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public ResponseEntity<AppointmentResponse> createWalkInPaidAppointment(
            @Valid @RequestBody CreateWalkInAppointmentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.createWalkInPaidAppointment(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getAll(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorScheduleId,
            @RequestParam(required = false) AppointmentStatus status
    ) {
        return ResponseEntity.ok(appointmentService.getAll(patientId, doctorScheduleId, status));
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
            @RequestBody SepayWebhookRequest request
    ) {
        AppointmentResponse response = appointmentService.confirmPaymentFromSepayWebhook(request, secretKeyHeader);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "success", true,
                "appointmentId", response.getAppointmentId(),
                "appointmentCode", response.getAppointmentCode()
        ));
    }
}
