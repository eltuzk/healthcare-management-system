package com.healthcare.backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.healthcare.backend.dto.request.RoleRequestDTO;
import com.healthcare.backend.dto.response.PermissionResponseDTO;
import com.healthcare.backend.dto.response.RoleResponseDTO;
import com.healthcare.backend.entity.Permission;
import com.healthcare.backend.entity.Role;
import com.healthcare.backend.entity.RolePermission;
import com.healthcare.backend.entity.RolePermissionId;
import com.healthcare.backend.repository.PermissionRepository;
import com.healthcare.backend.repository.RolePermissionRepository;
import com.healthcare.backend.repository.RoleRepository;
import com.healthcare.backend.service.RoleServiceInterface;

@Service
public class RoleServiceImpl implements RoleServiceInterface {
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private RolePermissionRepository rolePermissionRepository;
    @Autowired
    private PermissionRepository permissionRepository;
    
    @Override
    public Page<RoleResponseDTO> getAllRoles(Pageable pageable) {
        return roleRepository.findAll(pageable)
            .map(role -> new RoleResponseDTO(role.getRoleId(), role.getRoleName(), role.getDescription()));
    }

    @Override
    public RoleResponseDTO getRoleById(Long id) {
        return roleRepository.findById(id)
            .map(role -> new RoleResponseDTO(role.getRoleId(), role.getRoleName(), role.getDescription()))
            .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
    }

    @Override
    public RoleResponseDTO createRole(RoleRequestDTO roleRequestDTO) {
        if (roleRepository.existsByRoleName(roleRequestDTO.getRoleName().toUpperCase())) {
            throw new RuntimeException("Role name already exists: " + roleRequestDTO.getRoleName());
        }

        Role temp = new Role();
        temp.setRoleName(roleRequestDTO.getRoleName().toUpperCase());
        temp.setDescription(roleRequestDTO.getDescription());

        Role savedRole = roleRepository.save(temp);
        return new RoleResponseDTO(savedRole.getRoleId(), savedRole.getRoleName(), savedRole.getDescription());
    }

    @Override
    public RoleResponseDTO updateRole(Long id, RoleRequestDTO roleRequestDTO) {
        Role existingRole = roleRepository.findById(id).orElse(null);
        if (existingRole == null) {
            throw new RuntimeException("Role not found with id: " + id);
        }

        existingRole.setRoleName(roleRequestDTO.getRoleName().toUpperCase());
        existingRole.setDescription(roleRequestDTO.getDescription());

        Role updatedRole = roleRepository.save(existingRole);
        return new RoleResponseDTO(updatedRole.getRoleId(), updatedRole.getRoleName(), updatedRole.getDescription());
    }

    @Override
    public void deleteRole (Long id) {
        Role existingRole = roleRepository.findById(id).orElse(null);
        if(existingRole == null) {
            throw new RuntimeException("Role not found with id: " + id);
        }

        boolean isUsed = rolePermissionRepository.existsByRole_RoleId(id);
        if (isUsed) {
            throw new RuntimeException("Cannot delete role: This role is currently assigned and has existing permissions.");
        }

        roleRepository.deleteById(id);
        return;
    }

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
