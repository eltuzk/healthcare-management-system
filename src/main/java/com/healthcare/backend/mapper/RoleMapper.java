package com.healthcare.backend.mapper;

import org.springframework.stereotype.Component;

import com.healthcare.backend.dto.request.RoleRequest;
import com.healthcare.backend.dto.response.RoleResponse;
import com.healthcare.backend.entity.Role;

@Component
public class RoleMapper {

    public Role toEntity(RoleRequest request) {
        Role entity = new Role();
        entity.setRoleName(request.getRoleName());
        entity.setDescription(request.getDescription());

        return entity;
    }

    public RoleResponse toResponse(Role entity) {
        RoleResponse response = new RoleResponse();
        response.setRoleId(entity.getRoleId());
        response.setRoleName(entity.getRoleName());
        response.setDescription(entity.getDescription());

        return response;
    }

    public void updateEntityFromRequest(RoleRequest request, Role entity) {
        if (request.getRoleName() != null) entity.setRoleName(request.getRoleName());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
    }
}
