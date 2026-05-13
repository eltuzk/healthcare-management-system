package com.healthcare.backend.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import com.healthcare.backend.dto.request.PharmacistRequest;
import com.healthcare.backend.dto.response.PharmacistResponse;

public interface PharmacistService {

    Page<PharmacistResponse> getAll(Pageable pageable);

    PharmacistResponse getById(Long id);

    PharmacistResponse create(PharmacistRequest request);

    PharmacistResponse update(Long id, PharmacistRequest request);

    PharmacistResponse getMe(String email);

    PharmacistResponse updateMe(String email, PharmacistRequest request);

    void delete(Long id);
}