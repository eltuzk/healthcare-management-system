package com.healthcare.backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.healthcare.backend.dto.request.RoleRequest;
import com.healthcare.backend.dto.response.PermissionResponse;
import com.healthcare.backend.dto.response.RoleResponse;
import com.healthcare.backend.entity.Permission;
import com.healthcare.backend.entity.Role;
import com.healthcare.backend.entity.RolePermission;
import com.healthcare.backend.entity.RolePermissionId;
import com.healthcare.backend.mapper.PermissionMapper;
import com.healthcare.backend.mapper.RoleMapper;
import com.healthcare.backend.repository.AccountRepository;
import com.healthcare.backend.repository.PermissionRepository;
import com.healthcare.backend.repository.RolePermissionRepository;
import com.healthcare.backend.repository.RoleRepository;
import com.healthcare.backend.service.RoleService;

@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private RolePermissionRepository rolePermissionRepository;
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private PermissionMapper permissionMapper;

    @Override
    public Page<RoleResponse> getAllRoles(Pageable pageable) {
        return roleRepository.findAll(pageable)
                .map(roleMapper::toRoleResponse);
    }

    @Override
    public RoleResponse getRoleById(Long id) {
        return roleRepository.findById(id)
                .map(roleMapper::toRoleResponse)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
    }

    @Override
    public RoleResponse createRole(RoleRequest roleRequest) {
        if (roleRepository.existsByRoleName(roleRequest.getRoleName().toUpperCase())) {
            throw new RuntimeException("Role name already exists: " + roleRequest.getRoleName());
        }

        Role temp = roleMapper.toRoleEntity(roleRequest);
        temp.setRoleName(temp.getRoleName().toUpperCase());

        Role savedRole = roleRepository.save(temp);
        return roleMapper.toRoleResponse(savedRole);
    }

    @Override
    public RoleResponse updateRole(Long id, RoleRequest roleRequest) {
        Role existingRole = roleRepository.findById(id).orElse(null);
        if (existingRole == null) {
            throw new RuntimeException("Role not found with id: " + id);
        }

        if (roleRepository.existsByRoleNameAndRoleIdNot(roleRequest.getRoleName(), id)) {
            throw new RuntimeException("Role name already exists: " + roleRequest.getRoleName());
        }
        ;
        roleMapper.updateRoleEntity(roleRequest, existingRole);
        existingRole.setRoleName(existingRole.getRoleName().toUpperCase());

        Role updatedRole = roleRepository.save(existingRole);
        return roleMapper.toRoleResponse(updatedRole);
    }

    @Override
    public void deleteRole(Long id) {
        Role existingRole = roleRepository.findById(id).orElse(null);
        if (existingRole == null) {
            throw new RuntimeException("Role not found with id: " + id);
        }

        boolean isUsedByAccounts = accountRepository.existsByRole_RoleId(id);
        if (isUsedByAccounts) {
            throw new RuntimeException("Cannot delete role: This role is currently assigned to existing accounts.");
        }

        boolean isUsedByPermissions = rolePermissionRepository.existsByRole_RoleId(id);
        if (isUsedByPermissions) {
            throw new RuntimeException(
                    "Cannot delete role: This role is currently assigned and has existing permissions.");
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
    public Page<PermissionResponse> getPermissionsOfRole(Long roleId, Pageable pageable) {
        roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

        return rolePermissionRepository.findAllByRole_RoleId(roleId, pageable)
                .map(rolePermission -> permissionMapper.toPermissionResponse(rolePermission.getPermission()));
    }
}
