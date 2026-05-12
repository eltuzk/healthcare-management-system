package com.healthcare.backend.controller;

import com.healthcare.backend.dto.request.PharmacistRequest;
import com.healthcare.backend.dto.response.PharmacistResponse;
import com.healthcare.backend.service.PharmacistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pharmacists")
@RequiredArgsConstructor
public class PharmacistController {

    private final PharmacistService pharmacistService;

    @GetMapping
    public ResponseEntity<Page<PharmacistResponse>> getAllPharmacists(Pageable pageable) {
        return ResponseEntity.ok(pharmacistService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PharmacistResponse> getPharmacistById(@PathVariable Long id) {
        return ResponseEntity.ok(pharmacistService.getById(id));
    }

    @PostMapping
    public ResponseEntity<PharmacistResponse> createPharmacist(@Valid @RequestBody PharmacistRequest request) {
        PharmacistResponse response = pharmacistService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PharmacistResponse> updatePharmacist(
            @PathVariable Long id, 
            @Valid @RequestBody PharmacistRequest request) {
        return ResponseEntity.ok(pharmacistService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePharmacist(@PathVariable Long id) {
        pharmacistService.delete(id);
        return ResponseEntity.noContent().build();
    }
}