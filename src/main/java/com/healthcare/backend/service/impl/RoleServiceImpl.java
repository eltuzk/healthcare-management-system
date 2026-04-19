package com.healthcare.backend.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.healthcare.backend.dto.request.RoleRequest;
import com.healthcare.backend.dto.response.RoleResponse;
import com.healthcare.backend.entity.Role;
import com.healthcare.backend.exception.BusinessException;
import com.healthcare.backend.exception.DuplicateResourceException;
import com.healthcare.backend.exception.ResourceNotFoundException;
import com.healthcare.backend.mapper.RoleMapper;
import com.healthcare.backend.repository.AccountRepository;
import com.healthcare.backend.repository.RoleRepository;
import com.healthcare.backend.service.RoleService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAll() {
        return roleRepository.findAll().stream().map(roleMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getById(Long id) {
        return roleMapper.toResponse(findOrThrow(id));
    }

    @Override
    public RoleResponse create(RoleRequest request) {
        if (roleRepository.existsByRoleName(request.getRoleName())) {
            throw new DuplicateResourceException("Role name already exists: " + request.getRoleName());
        }

        return roleMapper.toResponse(roleRepository.save(roleMapper.toEntity(request)));
    }

    @Override
    public RoleResponse update(Long id, RoleRequest request) {
        Role entity = findOrThrow(id);

        if (roleRepository.existsByRoleNameAndRoleIdNot(request.getRoleName(), id)) {
            throw new DuplicateResourceException("Role name already exists: " + request.getRoleName());
        }

        roleMapper.updateEntityFromRequest(request, entity);

        return roleMapper.toResponse(roleRepository.save(entity));
    }

    @Override
    public void delete(Long id) {
        findOrThrow(id);

        if (accountRepository.existsByRole_RoleId(id)) {
            throw new BusinessException("Cannot delete role: it is assigned to one or more accounts");
        }

        roleRepository.deleteById(id);
    }

    private Role findOrThrow(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + id));
    }
}
