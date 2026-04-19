package com.healthcare.backend.service;

import com.healthcare.backend.dto.request.RoomRequest;
import com.healthcare.backend.dto.response.RoomResponse;

import java.util.List;

public interface RoomService {

    List<RoomResponse> getAllRooms(Long roomTypeId);

    RoomResponse getRoomById(Long id);

    RoomResponse createRoom(RoomRequest request);

    RoomResponse updateRoom(Long id, RoomRequest request);

    void deleteRoom(Long id);
}