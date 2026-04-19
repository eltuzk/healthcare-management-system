package com.healthcare.backend.controller;

import com.healthcare.backend.dto.request.RoomTypeRequestDTO;
import com.healthcare.backend.dto.response.RoomTypeResponseDTO;
import com.healthcare.backend.service.RoomTypeServiceInterface;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/room-types")
public class RoomTypeController {

    @Autowired
    private RoomTypeServiceInterface roomTypeService;

    @GetMapping
    public ResponseEntity<List<RoomTypeResponseDTO>> getAllRoomTypes() {
        return ResponseEntity.ok(roomTypeService.getAllRoomTypes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomTypeResponseDTO> getRoomTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(roomTypeService.getRoomTypeById(id));
    }

    @PostMapping
    public ResponseEntity<RoomTypeResponseDTO> createRoomType(@Valid @RequestBody RoomTypeRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomTypeService.createRoomType(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomTypeResponseDTO> updateRoomType(@PathVariable Long id,
                                                              @Valid @RequestBody RoomTypeRequestDTO request) {
        return ResponseEntity.ok(roomTypeService.updateRoomType(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoomType(@PathVariable Long id) {
        roomTypeService.deleteRoomType(id);
        return ResponseEntity.noContent().build();
    }
}