package com.healthcare.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.healthcare.backend.dto.response.PermissionResponseDTO;

public interface RolePermissionServiceInterface {
    void addPermissisonToRole(Long roleId, Long permissionId);

    void removePermissionFromRole(Long roleId, Long permissionId);

    Page<PermissionResponseDTO> getPermissionsOfRole(Long roleId, Pageable pageable);
}
