package com.healthcare.backend.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthcare.backend.dto.request.RolePermissionRequest;
import com.healthcare.backend.dto.response.RolePermissionResponse;
import com.healthcare.backend.entity.Permission;
import com.healthcare.backend.entity.Role;
import com.healthcare.backend.entity.RolePermission;
import com.healthcare.backend.entity.RolePermissionId;
import com.healthcare.backend.exception.DuplicateResourceException;
import com.healthcare.backend.exception.ResourceNotFoundException;
import com.healthcare.backend.mapper.RolePermissionMapper;
import com.healthcare.backend.repository.PermissionRepository;
import com.healthcare.backend.repository.RolePermissionRepository;
import com.healthcare.backend.repository.RoleRepository;
import com.healthcare.backend.service.RolePermissionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RolePermissionServiceImpl implements RolePermissionService {

    private final RolePermissionRepository rolePermissionRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionMapper rolePermissionMapper;

    @Override
    public RolePermissionResponse assign(RolePermissionRequest request) {
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRoleId()));

        Permission permission = permissionRepository.findById(request.getPermissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + request.getPermissionId()));

        RolePermissionId id = new RolePermissionId();
        id.setRoleId(request.getRoleId());
        id.setPermissionId(request.getPermissionId());

        if (rolePermissionRepository.existsById(id)) {
            throw new DuplicateResourceException("Role already has this permission");
        }

        RolePermission entity = new RolePermission();
        entity.setRolePermissionId(id);
        entity.setRole(role);
        entity.setPermission(permission);

        return rolePermissionMapper.toResponse(rolePermissionRepository.save(entity));
    }

    @Override
    public void revoke(RolePermissionRequest request) {
        RolePermissionId id = new RolePermissionId();
        id.setRoleId(request.getRoleId());
        id.setPermissionId(request.getPermissionId());

        if (!rolePermissionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Role does not have this permission");
        }

        rolePermissionRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolePermissionResponse> getByRoleId(Long roleId) {
        roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));

        return rolePermissionRepository.findAllByRole_RoleId(roleId).stream()
                .map(rolePermissionMapper::toResponse)
                .toList();
    }
}
