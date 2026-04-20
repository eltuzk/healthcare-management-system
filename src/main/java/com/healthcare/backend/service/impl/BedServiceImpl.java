package com.healthcare.backend.service.impl;

import com.healthcare.backend.dto.request.BedRequest;
import com.healthcare.backend.dto.response.BedResponse;
import com.healthcare.backend.entity.Bed;
import com.healthcare.backend.entity.Room;
import com.healthcare.backend.repository.BedRepository;
import com.healthcare.backend.repository.RoomRepository;
import com.healthcare.backend.service.BedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BedServiceImpl implements BedService {

    @Autowired
    private BedRepository bedRepository;

    @Autowired
    private RoomRepository roomRepository;

    private BedResponse toDTO(Bed bed) {
        return new BedResponse(
                bed.getBedId(),
                bed.getPrice(),
                bed.getStatus().name(),
                bed.getRoom().getRoomId()
        );
    }

    @Override
    public List<BedResponse> getBedsByRoom(Long roomId) {
        roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        return bedRepository.findByRoom_RoomId(roomId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BedResponse addBed(Long roomId, BedRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        Bed bed = new Bed();
        bed.setPrice(request.getPrice());
        bed.setStatus(Bed.BedStatus.AVAILABLE);
        bed.setRoom(room);

        Bed saved = bedRepository.save(bed);
        return toDTO(saved);
    }

    @Override
    public BedResponse updateBed(Long bedId, BedRequest request) {
        Bed existing = bedRepository.findById(bedId)
                .orElseThrow(() -> new RuntimeException("Bed not found with id: " + bedId));

        existing.setPrice(request.getPrice());

        Bed updated = bedRepository.save(existing);
        return toDTO(updated);
    }

    @Override
    public void deleteBed(Long bedId) {
        bedRepository.findById(bedId)
                .orElseThrow(() -> new RuntimeException("Bed not found with id: " + bedId));

        bedRepository.deleteById(bedId);
    }
}