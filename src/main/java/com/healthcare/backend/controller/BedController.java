package com.healthcare.backend.controller;

import com.healthcare.backend.dto.request.BedRequestDTO;
import com.healthcare.backend.dto.response.BedResponseDTO;
import com.healthcare.backend.service.BedServiceInterface;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BedController {

    @Autowired
    private BedServiceInterface bedService;

    @GetMapping("/rooms/{roomId}/beds")
    public ResponseEntity<List<BedResponseDTO>> getBedsByRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(bedService.getBedsByRoom(roomId));
    }

    @PostMapping("/rooms/{roomId}/beds")
    public ResponseEntity<BedResponseDTO> addBed(@PathVariable Long roomId,
                                                 @Valid @RequestBody BedRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bedService.addBed(roomId, request));
    }

    @PutMapping("/beds/{id}")
    public ResponseEntity<BedResponseDTO> updateBed(@PathVariable Long id,
                                                    @Valid @RequestBody BedRequestDTO request) {
        return ResponseEntity.ok(bedService.updateBed(id, request));
    }

    @DeleteMapping("/beds/{id}")
    public ResponseEntity<Void> deleteBed(@PathVariable Long id) {
        bedService.deleteBed(id);
        return ResponseEntity.noContent().build();
    }
}