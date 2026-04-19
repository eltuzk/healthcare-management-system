package com.healthcare.backend.service;

import com.healthcare.backend.dto.request.RoomRequestDTO;
import com.healthcare.backend.dto.response.RoomResponseDTO;

import java.util.List;

public interface RoomServiceInterface {

    List<RoomResponseDTO> getAllRooms(Long roomTypeId);

    RoomResponseDTO getRoomById(Long id);

    RoomResponseDTO createRoom(RoomRequestDTO request);

    RoomResponseDTO updateRoom(Long id, RoomRequestDTO request);

    void deleteRoom(Long id);
}