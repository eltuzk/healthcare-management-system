package com.healthcare.backend.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthcare.backend.dto.request.PermissionRequest;
import com.healthcare.backend.dto.response.PermissionResponse;
import com.healthcare.backend.entity.Permission;
import com.healthcare.backend.exception.BusinessException;
import com.healthcare.backend.exception.DuplicateResourceException;
import com.healthcare.backend.exception.ResourceNotFoundException;
import com.healthcare.backend.mapper.PermissionMapper;
import com.healthcare.backend.repository.AccountPermissionRepository;
import com.healthcare.backend.repository.PermissionRepository;
import com.healthcare.backend.repository.RolePermissionRepository;
import com.healthcare.backend.service.PermissionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;
    private final RolePermissionRepository rolePermissionRepository;
    private final AccountPermissionRepository accountPermissionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAll() {
        return permissionRepository.findAll().stream().map(permissionMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionResponse getById(Long id) {
        return permissionMapper.toResponse(findOrThrow(id));
    }

    @Override
    public PermissionResponse create(PermissionRequest request) {
        if (permissionRepository.existsByPermissionName(request.getPermissionName())) {
            throw new DuplicateResourceException("Permission name already exists: " + request.getPermissionName());
        }

        return permissionMapper.toResponse(permissionRepository.save(permissionMapper.toEntity(request)));
    }

    @Override
    public PermissionResponse update(Long id, PermissionRequest request) {
        Permission entity = findOrThrow(id);

        if (permissionRepository.existsByPermissionNameAndPermissionIdNot(request.getPermissionName(), id)) {
            throw new DuplicateResourceException("Permission name already exists: " + request.getPermissionName());
        }

        permissionMapper.updateEntityFromRequest(request, entity);

        return permissionMapper.toResponse(permissionRepository.save(entity));
    }

    @Override
    public void delete(Long id) {
        findOrThrow(id);

        if (rolePermissionRepository.existsByPermission_PermissionId(id)) {
            throw new BusinessException("Cannot delete permission: it is assigned to one or more roles");
        }

        if (accountPermissionRepository.existsByPermission_PermissionId(id)) {
            throw new BusinessException("Cannot delete permission: it is assigned to one or more accounts");
        }

        permissionRepository.deleteById(id);
    }

    private Permission findOrThrow(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + id));
    }
}
