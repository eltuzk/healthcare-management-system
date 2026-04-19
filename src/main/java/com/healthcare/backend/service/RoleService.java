package com.healthcare.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.healthcare.backend.dto.request.RoleRequest;
import com.healthcare.backend.dto.response.PermissionResponse;
import com.healthcare.backend.dto.response.RoleResponse;

public interface RoleService {
    Page<RoleResponse> getAllRoles (Pageable pageable);

    RoleResponse getRoleById (Long id);

    RoleResponse createRole (RoleRequest roleRequest);

    RoleResponse updateRole (Long id, RoleRequest roleRequest);

    void deleteRole (Long id);

    void addPermissionToRole(Long roleId, Long permissionId);

    void removePermissionFromRole(Long roleId, Long permissionId);

    Page<PermissionResponse> getPermissionsOfRole(Long roleId, Pageable pageable);
}