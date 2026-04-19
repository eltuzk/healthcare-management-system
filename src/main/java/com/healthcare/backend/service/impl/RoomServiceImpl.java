package com.healthcare.backend.service.impl;

import com.healthcare.backend.dto.request.RoomRequestDTO;
import com.healthcare.backend.dto.response.RoomResponseDTO;
import com.healthcare.backend.entity.Branch;
import com.healthcare.backend.entity.Room;
import com.healthcare.backend.entity.RoomType;
import com.healthcare.backend.repository.BedRepository;
import com.healthcare.backend.repository.BranchRepository;
import com.healthcare.backend.repository.RoomRepository;
import com.healthcare.backend.repository.RoomTypeRepository;
import com.healthcare.backend.service.RoomServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomServiceInterface {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private BedRepository bedRepository;

    private RoomResponseDTO toDTO(Room room) {
        return new RoomResponseDTO(
                room.getRoomId(),
                room.getRoomCode(),
                room.getPosition(),
                room.getNote(),
                room.getRoomType().getRoomTypeId(),
                room.getRoomType().getRoomTypeName(),
                room.getBranch().getBranchId(),
                room.getBranch().getBranchName()
        );
    }

    @Override
    public List<RoomResponseDTO> getAllRooms(Long roomTypeId) {
        List<Room> rooms = (roomTypeId != null)
                ? roomRepository.findByRoomType_RoomTypeId(roomTypeId)
                : roomRepository.findAll();

        return rooms.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RoomResponseDTO getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));
        return toDTO(room);
    }

    @Override
    public RoomResponseDTO createRoom(RoomRequestDTO request) {
        if (roomRepository.existsByRoomCode(request.getRoomCode())) {
            throw new RuntimeException("Room code already exists: " + request.getRoomCode());
        }

        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new RuntimeException("Room type not found with id: " + request.getRoomTypeId()));

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + request.getBranchId()));

        Room room = new Room();
        room.setRoomCode(request.getRoomCode());
        room.setPosition(request.getPosition());
        room.setNote(request.getNote());
        room.setRoomType(roomType);
        room.setBranch(branch);

        Room saved = roomRepository.save(room);
        return toDTO(saved);
    }

    @Override
    public RoomResponseDTO updateRoom(Long id, RoomRequestDTO request) {
        Room existing = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));

        if (roomRepository.existsByRoomCodeAndRoomIdNot(request.getRoomCode(), id)) {
            throw new RuntimeException("Room code already exists: " + request.getRoomCode());
        }

        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new RuntimeException("Room type not found with id: " + request.getRoomTypeId()));

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + request.getBranchId()));

        existing.setRoomCode(request.getRoomCode());
        existing.setPosition(request.getPosition());
        existing.setNote(request.getNote());
        existing.setRoomType(roomType);
        existing.setBranch(branch);

        Room updated = roomRepository.save(existing);
        return toDTO(updated);
    }

    @Override
    public void deleteRoom(Long id) {
        roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));

        if (bedRepository.existsByRoom_RoomId(id)) {
            throw new RuntimeException("Cannot delete room: There are beds currently in this room.");
        }

        roomRepository.deleteById(id);
    }
}