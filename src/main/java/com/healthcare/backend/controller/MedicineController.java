package com.healthcare.backend.controller;

import com.healthcare.backend.dto.request.MedicineRequest;
import com.healthcare.backend.dto.response.MedicineResponse;
import com.healthcare.backend.service.MedicineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/medicines")
public class MedicineController {

    private final MedicineService medicineService;

    @GetMapping
    public ResponseEntity<List<MedicineResponse>> getAllMedicines() {
        return ResponseEntity.ok(medicineService.getAllMedicines());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicineResponse> getMedicineById(@PathVariable Long id) {
        return ResponseEntity.ok(medicineService.getMedicineById(id));
    }

    @PostMapping
    public ResponseEntity<MedicineResponse> createMedicine(@Valid @RequestBody MedicineRequest request) {
        MedicineResponse response = medicineService.createMedicine(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicineResponse> updateMedicine(
            @PathVariable Long id,
            @Valid @RequestBody MedicineRequest request
    ) {
        return ResponseEntity.ok(medicineService.updateMedicine(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MedicineResponse> deactivateMedicine(@PathVariable Long id) {
        return ResponseEntity.ok(medicineService.deactivateMedicine(id));
    }
}