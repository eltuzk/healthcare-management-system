package com.healthcare.backend.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springdoc.core.converters.models.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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

import com.healthcare.backend.dto.request.DoctorRequestDTO;
import com.healthcare.backend.dto.response.DoctorResponseDTO;
import com.healthcare.backend.service.DoctorServiceInterface;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/doctors")
public class DoctorController {
    @Autowired
    private DoctorServiceInterface doctorService;

    @GetMapping
    public ResponseEntity<Page<DoctorResponseDTO>> getAllDoctors(
        @ParameterObject Pageable pageable,
        @RequestParam(required = false) String specialization
    ) {
        Page<DoctorResponseDTO> res = doctorService.getAllDoctors(null, specialization);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{doctorId}")
    public ResponseEntity<DoctorResponseDTO> getDoctorById(@PathVariable Long doctorId) {
        DoctorResponseDTO res = doctorService.getDoctorById(doctorId);
        return ResponseEntity.ok(res);
    }

    @PostMapping
    public ResponseEntity<DoctorResponseDTO> createDoctor(@Valid @RequestBody DoctorRequestDTO doctorRequest) {
        DoctorResponseDTO res = doctorService.createDoctor(doctorRequest);
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{doctorId}")
    public ResponseEntity<DoctorResponseDTO> updateDoctor(
        @PathVariable Long doctorId,
        @Valid @RequestBody DoctorRequestDTO doctorRequest
    ) {
        DoctorResponseDTO res = doctorService.updateDoctor(doctorRequest, doctorId);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{doctorId}")
    public ResponseEntity<String> deleteDoctor(@PathVariable Long doctorId) {
        doctorService.deleteDoctor(doctorId);
        return ResponseEntity.ok("Deleted successfully.");
    }
}
