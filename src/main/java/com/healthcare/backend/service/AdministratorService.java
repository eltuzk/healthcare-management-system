package com.healthcare.backend.service;

import com.healthcare.backend.dto.request.AdministratorRequest;
import com.healthcare.backend.dto.response.AdministratorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdministratorService {
    Page<AdministratorResponse> getAll(Pageable pageable);
    AdministratorResponse getById(Long adminId);
    AdministratorResponse getMe(String email);
    AdministratorResponse updateMe(String email, AdministratorRequest request);
    AdministratorResponse create(AdministratorRequest request);
    AdministratorResponse update(Long adminId, AdministratorRequest request);
    void delete(Long adminId);
}
