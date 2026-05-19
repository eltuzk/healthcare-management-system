package com.healthcare.backend.controller;

import com.healthcare.backend.dto.request.MedicineLotRequest;
import com.healthcare.backend.dto.response.MedicineLotResponse;
import com.healthcare.backend.service.MedicineLotService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/medicine-lots")
public class MedicineLotController {

    private final MedicineLotService medicineLotService;

    @GetMapping
    public ResponseEntity<List<MedicineLotResponse>> getAllMedicineLots(
            @RequestParam(required = false) Long medicineId
    ) {
        return ResponseEntity.ok(medicineLotService.getAllMedicineLots(medicineId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicineLotResponse> getMedicineLotById(@PathVariable Long id) {
        return ResponseEntity.ok(medicineLotService.getMedicineLotById(id));
    }

    @PostMapping
    public ResponseEntity<MedicineLotResponse> createMedicineLot(
            @Valid @RequestBody MedicineLotRequest request
    ) {
        MedicineLotResponse response = medicineLotService.createMedicineLot(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicineLotResponse> updateMedicineLot(
            @PathVariable Long id,
            @Valid @RequestBody MedicineLotRequest request
    ) {
        return ResponseEntity.ok(medicineLotService.updateMedicineLot(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MedicineLotResponse> deactivateMedicineLot(@PathVariable Long id) {
        return ResponseEntity.ok(medicineLotService.deactivateMedicineLot(id));
    }
}