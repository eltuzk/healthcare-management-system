package com.healthcare.backend.service;

import com.healthcare.backend.dto.request.RoomTypeRequest;
import com.healthcare.backend.dto.response.RoomTypeResponse;

import java.util.List;

public interface RoomTypeService {

    List<RoomTypeResponse> getAllRoomTypes();

    RoomTypeResponse getRoomTypeById(Long id);

    RoomTypeResponse createRoomType(RoomTypeRequest request);

    RoomTypeResponse updateRoomType(Long id, RoomTypeRequest request);

    void deleteRoomType(Long id);
}