package com.healthcare.backend.service;

import com.healthcare.backend.dto.request.ReceptionistRequest;
import com.healthcare.backend.dto.response.ReceptionistResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReceptionistService {

    Page<ReceptionistResponse> getAll(Pageable pageable);

    ReceptionistResponse getById(Long id);

    ReceptionistResponse create(ReceptionistRequest request);

    ReceptionistResponse update(Long id, ReceptionistRequest request);

    void delete(Long id);
}