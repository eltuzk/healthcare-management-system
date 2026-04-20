package com.healthcare.backend.service.impl;

import com.healthcare.backend.dto.request.RoomTypeRequest;
import com.healthcare.backend.dto.response.RoomTypeResponse;
import com.healthcare.backend.entity.RoomType;
import com.healthcare.backend.repository.RoomRepository;
import com.healthcare.backend.repository.RoomTypeRepository;
import com.healthcare.backend.service.RoomTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomTypeServiceImpl implements RoomTypeService {

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Override
    public List<RoomTypeResponse> getAllRoomTypes() {
        return roomTypeRepository.findAll()
                .stream()
                .map(rt -> new RoomTypeResponse(rt.getRoomTypeId(), rt.getRoomTypeName()))
                .collect(Collectors.toList());
    }

    @Override
    public RoomTypeResponse getRoomTypeById(Long id) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room type not found with id: " + id));
        return new RoomTypeResponse(roomType.getRoomTypeId(), roomType.getRoomTypeName());
    }

    @Override
    public RoomTypeResponse createRoomType(RoomTypeRequest request) {
        if (roomTypeRepository.existsByRoomTypeName(request.getRoomTypeName())) {
            throw new RuntimeException("Room type name already exists: " + request.getRoomTypeName());
        }

        RoomType roomType = new RoomType();
        roomType.setRoomTypeName(request.getRoomTypeName());

        RoomType saved = roomTypeRepository.save(roomType);
        return new RoomTypeResponse(saved.getRoomTypeId(), saved.getRoomTypeName());
    }

    @Override
    public RoomTypeResponse updateRoomType(Long id, RoomTypeRequest request) {
        RoomType existing = roomTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room type not found with id: " + id));

        if (roomTypeRepository.existsByRoomTypeNameAndRoomTypeIdNot(request.getRoomTypeName(), id)) {
            throw new RuntimeException("Room type name already exists: " + request.getRoomTypeName());
        }

        existing.setRoomTypeName(request.getRoomTypeName());

        RoomType updated = roomTypeRepository.save(existing);
        return new RoomTypeResponse(updated.getRoomTypeId(), updated.getRoomTypeName());
    }

    @Override
    public void deleteRoomType(Long id) {
        roomTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room type not found with id: " + id));

        if (roomRepository.existsByRoomType_RoomTypeId(id)) {
            throw new RuntimeException("Cannot delete room type: There are rooms currently using this room type.");
        }

        roomTypeRepository.deleteById(id);
    }
}