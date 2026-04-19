package com.healthcare.backend.service;

import java.util.List;

import com.healthcare.backend.dto.request.PermissionRequest;
import com.healthcare.backend.dto.response.PermissionResponse;

public interface PermissionService {

    List<PermissionResponse> getAll();

    PermissionResponse getById(Long id);

    PermissionResponse create(PermissionRequest request);

    PermissionResponse update(Long id, PermissionRequest request);

    void delete(Long id);
}
