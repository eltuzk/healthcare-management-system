package com.healthcare.backend.service;

import com.healthcare.backend.dto.request.BedRequest;
import com.healthcare.backend.dto.response.BedResponse;

import java.util.List;

public interface BedService {

    List<BedResponse> getBedsByRoom(Long roomId);

    BedResponse addBed(Long roomId, BedRequest request);

    BedResponse updateBed(Long bedId, BedRequest request);

    void deleteBed(Long bedId);
}