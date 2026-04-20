package com.healthcare.backend.mapper;

import org.springframework.stereotype.Component;

import com.healthcare.backend.dto.request.PermissionRequest;
import com.healthcare.backend.dto.response.PermissionResponse;
import com.healthcare.backend.entity.Permission;

@Component
public class PermissionMapper {

    public Permission toEntity(PermissionRequest request) {
        Permission entity = new Permission();
        entity.setPermissionName(request.getPermissionName());
        entity.setDetail(request.getDetail());

        return entity;
    }

    public PermissionResponse toResponse(Permission entity) {
        PermissionResponse response = new PermissionResponse();
        response.setPermissionId(entity.getPermissionId());
        response.setPermissionName(entity.getPermissionName());
        response.setDetail(entity.getDetail());

        return response;
    }

    public void updateEntityFromRequest(PermissionRequest request, Permission entity) {
        if (request.getPermissionName() != null) entity.setPermissionName(request.getPermissionName());
        if (request.getDetail() != null) entity.setDetail(request.getDetail());
    }
}
