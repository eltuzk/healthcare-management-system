package com.healthcare.backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.healthcare.backend.entity.Role;
import com.healthcare.backend.entity.RolePermission;
import com.healthcare.backend.entity.RolePermissionId;
import com.healthcare.backend.dto.response.PermissionResponseDTO;
import com.healthcare.backend.entity.Permission;
import com.healthcare.backend.repository.PermissionRepository;
import com.healthcare.backend.repository.RolePermissionRepository;
import com.healthcare.backend.repository.RoleRepository;
import com.healthcare.backend.service.RolePermissionServiceInterface;

@Service
public class RolePermissionServiceImpl implements RolePermissionServiceInterface {
    @Autowired
    private RolePermissionRepository rolePermissionRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PermissionRepository permissionRepository;

    @Override
    public void addPermissisonToRole(Long roleId, Long permissionId) {
        Role tmp_Role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));
        
        Permission tmp_Permission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new RuntimeException("Permission not found with id: " + permissionId));

        RolePermissionId tmp_Id = new RolePermissionId(roleId, permissionId);
        if (rolePermissionRepository.existsById(tmp_Id)) {
            throw new RuntimeException("This permission has already been assigned to this role.");
        }

        RolePermission tmp_RolePermission = new RolePermission(tmp_Id, tmp_Role, tmp_Permission);
        rolePermissionRepository.save(tmp_RolePermission);
    }

    @Override
    public void removePermissionFromRole(Long roleId, Long permissionId) {
        roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));
        
        permissionRepository.findById(permissionId)
            .orElseThrow(() -> new RuntimeException("Permission not found with id: " + permissionId));

        RolePermissionId tmp_Id = new RolePermissionId(roleId, permissionId);
        if (!rolePermissionRepository.existsById(tmp_Id)) {
            throw new RuntimeException("This role does not have this permission.");
        }

        rolePermissionRepository.deleteById(tmp_Id);
    }

    @Override
    public Page<PermissionResponseDTO> getPermissionsOfRole(Long roleId, Pageable pageable) {
        roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

        return rolePermissionRepository.findAllByRole_RoleId(roleId, pageable)
            .map(rolePermission -> new PermissionResponseDTO(rolePermission.getPermission().getId(), rolePermission.getPermission().getPermissionName(), rolePermission.getPermission().getDetail()));
    }
}
