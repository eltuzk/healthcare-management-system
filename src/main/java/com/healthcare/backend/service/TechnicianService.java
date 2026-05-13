package com.healthcare.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.healthcare.backend.dto.request.TechnicianRequest;
import com.healthcare.backend.dto.response.TechnicianResponse;

public interface TechnicianService {

    Page<TechnicianResponse> getAll(Pageable pageable);

    TechnicianResponse getById(Long id);

    TechnicianResponse create(TechnicianRequest request);

    TechnicianResponse update(Long id, TechnicianRequest request);

    TechnicianResponse getMe(String email);

    TechnicianResponse updateMe(String email, TechnicianRequest request);

    void delete(Long id);
}