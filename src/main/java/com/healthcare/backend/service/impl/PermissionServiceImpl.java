package com.healthcare.backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.healthcare.backend.dto.request.PermissionRequest;
import com.healthcare.backend.dto.response.PermissionResponse;
import com.healthcare.backend.entity.Permission;
import com.healthcare.backend.mapper.PermissionMapper;
import com.healthcare.backend.repository.AccountPermissionRepository;
import com.healthcare.backend.repository.PermissionRepository;
import com.healthcare.backend.repository.RolePermissionRepository;
import com.healthcare.backend.service.PermissionService;

@Service
public class PermissionServiceImpl implements PermissionService {
    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private AccountPermissionRepository accountPermissionRepository;
    @Autowired
    private PermissionMapper permissionMapper;

    @Override
    public Page<PermissionResponse> getAllPermissions(Pageable pageable) {
        return permissionRepository.findAll(pageable)
                .map(permissionMapper::toPermissionResponse);
    }

    @Override
    public PermissionResponse getPermissionById(Long id) {
        return permissionRepository.findById(id)
                .map(permissionMapper::toPermissionResponse)
                .orElseThrow(() -> new RuntimeException("Permission not found with id: " + id));
    }

    @Override
    public PermissionResponse createPermission(PermissionRequest permissionRequest) {
        if (permissionRepository.existsByPermissionName(permissionRequest.getPermissionName())) {
            throw new RuntimeException("Permission name already exists: " + permissionRequest.getPermissionName());
        }

        Permission temp = permissionMapper.toPermissionEntity(permissionRequest);

        Permission res = permissionRepository.save(temp);
        return permissionMapper.toPermissionResponse(res);
    }

    @Override
    public void deletePermission(Long id) {
        Permission existingPermission = permissionRepository.findById(id).orElse(null);
        if (existingPermission == null) {
            throw new RuntimeException("Permission not found with id: " + id);
        }

        boolean isUsing_Role = rolePermissionRepository.existsByPermission_PermissionId(id);
        if (isUsing_Role) {
            throw new RuntimeException(
                    "Cannot delete permission: This permission is currently assigned and has been existing roles.");
        }

        boolean isUsing_Account = accountPermissionRepository.existsByPermission_PermissionId(id);
        if (isUsing_Account) {
            throw new RuntimeException(
                    "Cannot delete permission: This permission is currently assigned and has been existing accounts.");
        }

        permissionRepository.deleteById(id);
    }
}
