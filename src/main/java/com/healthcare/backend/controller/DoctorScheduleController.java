package com.healthcare.backend.controller;

import com.healthcare.backend.dto.request.CreateDoctorScheduleRequest;
import com.healthcare.backend.dto.request.UpdateDoctorScheduleRequest;
import com.healthcare.backend.dto.response.DoctorScheduleImportResponse;
import com.healthcare.backend.dto.response.DoctorScheduleResponse;
import com.healthcare.backend.service.DoctorScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/doctor-schedules")
@RequiredArgsConstructor
public class DoctorScheduleController {

    private final DoctorScheduleService doctorScheduleService;

    @PostMapping
    public ResponseEntity<DoctorScheduleResponse> create(@Valid @RequestBody CreateDoctorScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorScheduleService.create(request));
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DoctorScheduleImportResponse> importSchedules(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorScheduleService.importSchedules(file));
    }

    @GetMapping
    public ResponseEntity<List<DoctorScheduleResponse>> getAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long roomId
    ) {
        return ResponseEntity.ok(doctorScheduleService.getAll(date, doctorId, roomId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorScheduleResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorScheduleService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DoctorScheduleResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDoctorScheduleRequest request
    ) {
        return ResponseEntity.ok(doctorScheduleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        doctorScheduleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
