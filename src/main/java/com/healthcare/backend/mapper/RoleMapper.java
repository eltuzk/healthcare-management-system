package com.healthcare.backend.mapper;

import org.springframework.stereotype.Component;

import com.healthcare.backend.dto.request.RoleRequest;
import com.healthcare.backend.dto.response.RoleResponse;
import com.healthcare.backend.entity.Role;

@Component
public class RoleMapper {
    public RoleResponse toRoleResponse(Role role) {
        if (role == null) {
            return null;
        }

        return new RoleResponse(role.getRoleId(), role.getRoleName(), role.getDescription());
    }

    public Role toRoleEntity(RoleRequest roleRequest) {
        if (roleRequest == null) {
            return null;
        }

        Role role = new Role();
        role.setRoleName(roleRequest.getRoleName());
        role.setDescription(roleRequest.getDescription());
        return role;
    }

    public void updateRoleEntity(RoleRequest roleRequest, Role role) {
        if (roleRequest == null || role == null) {
            return;
        }

        role.setRoleName(roleRequest.getRoleName());
        role.setDescription(roleRequest.getDescription());
    }
}
