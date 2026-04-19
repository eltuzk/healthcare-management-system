package com.healthcare.backend.service;

import com.healthcare.backend.dto.request.RoomTypeRequestDTO;
import com.healthcare.backend.dto.response.RoomTypeResponseDTO;

import java.util.List;

public interface RoomTypeServiceInterface {

    List<RoomTypeResponseDTO> getAllRoomTypes();

    RoomTypeResponseDTO getRoomTypeById(Long id);

    RoomTypeResponseDTO createRoomType(RoomTypeRequestDTO request);

    RoomTypeResponseDTO updateRoomType(Long id, RoomTypeRequestDTO request);

    void deleteRoomType(Long id);
}