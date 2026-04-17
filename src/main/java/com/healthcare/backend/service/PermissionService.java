package com.healthcare.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.healthcare.backend.dto.request.PermissionRequest;
import com.healthcare.backend.dto.response.PermissionResponse;

public interface PermissionService {
    Page<PermissionResponse> getAllPermissions (Pageable pageable);

    PermissionResponse getPermissionById(Long id);

    PermissionResponse createPermission(PermissionRequest permissionRequest);

    void deletePermission(Long id);
}
