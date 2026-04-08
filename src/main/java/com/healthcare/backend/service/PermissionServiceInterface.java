package com.healthcare.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.healthcare.backend.dto.request.PermissionRequestDTO;
import com.healthcare.backend.dto.response.PermissionResponseDTO;

public interface PermissionServiceInterface {
    Page<PermissionResponseDTO> getAllPermission(Pageable pageable);

    PermissionResponseDTO getPermissionById(Long id);

    PermissionResponseDTO createPermission(PermissionRequestDTO permissionRequestDTO);

    void deletePermission(Long id);
}
