package com.healthcare.backend.service;

import com.healthcare.backend.dto.request.BedRequestDTO;
import com.healthcare.backend.dto.response.BedResponseDTO;

import java.util.List;

public interface BedServiceInterface {

    List<BedResponseDTO> getBedsByRoom(Long roomId);

    BedResponseDTO addBed(Long roomId, BedRequestDTO request);

    BedResponseDTO updateBed(Long bedId, BedRequestDTO request);

    void deleteBed(Long bedId);
}