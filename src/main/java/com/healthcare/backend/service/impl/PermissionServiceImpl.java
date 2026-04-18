package com.healthcare.backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.healthcare.backend.dto.request.PermissionRequestDTO;
import com.healthcare.backend.dto.response.PermissionResponseDTO;
import com.healthcare.backend.entity.Permission;
import com.healthcare.backend.repository.AccountPermissionRepository;
import com.healthcare.backend.repository.PermissionRepository;
import com.healthcare.backend.repository.RolePermissionRepository;
import com.healthcare.backend.service.PermissionServiceInterface;

@Service
public class PermissionServiceImpl implements PermissionServiceInterface {
    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private AccountPermissionRepository accountPermissionRepository;

    @Override
    public Page<PermissionResponseDTO> getAllPermissions(Pageable pageable) {
        return permissionRepository.findAll(pageable)
            .map(permission -> new PermissionResponseDTO(permission.getId(), permission.getPermissionName(), permission.getDetail()));
    }

    @Override
    public PermissionResponseDTO getPermissionById(Long id) {
        return permissionRepository.findById(id)
            .map(permission -> new PermissionResponseDTO(permission.getId(), permission.getPermissionName(), permission.getDetail()))
            .orElseThrow(() -> new RuntimeException("Permission not found with id: " + id));
    }

    @Override
    public PermissionResponseDTO createPermission(PermissionRequestDTO permissionRequestDTO) {
        if(permissionRepository.existsByPermissionName(permissionRequestDTO.getPermissionName())) {
            throw new RuntimeException("Permission name already exists: " + permissionRequestDTO.getPermissionName());
        }

        Permission temp = new Permission();
        temp.setPermissionName(permissionRequestDTO.getPermissionName());
        temp.setDetail(permissionRequestDTO.getDetails());

        Permission res = permissionRepository.save(temp);
        return new PermissionResponseDTO(res.getId(), res.getPermissionName(), res.getDetail());
    }

    @Override
    public void deletePermission(Long id) {
        Permission existingPermission = permissionRepository.findById(id).orElse(null);
        if (existingPermission == null) {
            throw new RuntimeException("Permission not found with id: " + id);
        }

        boolean isUsedByRole = rolePermissionRepository.existsByPermission_PermissionId(id);
        if (isUsedByRole) {
            throw new RuntimeException("Cannot delete permission: This permission is currently assigned and has been existing roles.");
        }

        boolean isUsedByAccount = accountPermissionRepository.existsByPermission_PermissionId(id);
        if (isUsedByAccount) {
            throw new RuntimeException("Cannot delete permission: This permission is currently assigned and has been existing accounts.");
        }

        permissionRepository.deleteById(id);
    }
}
