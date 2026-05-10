package com.healthcare.backend.controller;

import com.healthcare.backend.dto.request.ReceptionistRequest;
import com.healthcare.backend.dto.response.ReceptionistResponse;
import com.healthcare.backend.service.ReceptionistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/receptionists")
@RequiredArgsConstructor
public class ReceptionistController {

    private final ReceptionistService receptionistService;

    @GetMapping
    public ResponseEntity<Page<ReceptionistResponse>> getAllReceptionists(Pageable pageable) {
        return ResponseEntity.ok(receptionistService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReceptionistResponse> getReceptionistById(@PathVariable Long id) {
        return ResponseEntity.ok(receptionistService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ReceptionistResponse> createReceptionist(@Valid @RequestBody ReceptionistRequest request) {
        ReceptionistResponse response = receptionistService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReceptionistResponse> updateReceptionist(
            @PathVariable Long id, 
            @Valid @RequestBody ReceptionistRequest request) {
        return ResponseEntity.ok(receptionistService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReceptionist(@PathVariable Long id) {
        receptionistService.delete(id);
        return ResponseEntity.noContent().build();
    }
}