package com.healthcare.backend.mapper;

import org.springframework.stereotype.Component;

import com.healthcare.backend.dto.request.PermissionRequest;
import com.healthcare.backend.dto.response.PermissionResponse;
import com.healthcare.backend.entity.Permission;

@Component
public class PermissionMapper {
    public PermissionResponse toPermissionResponse(Permission permission) {
        if (permission == null) {
            return null;
        }

        return new PermissionResponse(permission.getPermissionId(), permission.getPermissionName(),
                permission.getDetail());
    }

    public Permission toPermissionEntity(PermissionRequest permissionRequest) {
        if (permissionRequest == null) {
            return null;
        }

        Permission permission = new Permission();
        permission.setPermissionName(permissionRequest.getPermissionName());
        permission.setDetail(permissionRequest.getDetail());
        return permission;
    }
}
