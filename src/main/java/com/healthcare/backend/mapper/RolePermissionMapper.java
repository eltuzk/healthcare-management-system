package com.healthcare.backend.mapper;

import org.springframework.stereotype.Component;

import com.healthcare.backend.dto.response.RolePermissionResponse;
import com.healthcare.backend.entity.RolePermission;

@Component
public class RolePermissionMapper {

    public RolePermissionResponse toResponse(RolePermission entity) {
        RolePermissionResponse response = new RolePermissionResponse();

        if (entity.getRolePermissionId() != null) {
            response.setRoleId(entity.getRolePermissionId().getRoleId());
            response.setPermissionId(entity.getRolePermissionId().getPermissionId());
        }

        if (entity.getRole() != null) {
            response.setRoleName(entity.getRole().getRoleName());
        }

        if (entity.getPermission() != null) {
            response.setPermissionName(entity.getPermission().getPermissionName());
        }

        return response;
    }
}
