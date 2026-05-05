package com.healthcare.backend.service;

import com.healthcare.backend.dto.request.SpecialtyRequest;
import com.healthcare.backend.dto.response.SpecialtyResponse;

import java.util.List;

public interface SpecialtyService {

    SpecialtyResponse create(SpecialtyRequest request);

    List<SpecialtyResponse> getAll();

    SpecialtyResponse getById(Long specialtyId);

    SpecialtyResponse update(Long specialtyId, SpecialtyRequest request);

    SpecialtyResponse deactivate(Long specialtyId);
}
