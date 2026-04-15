package com.healthcare.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.healthcare.backend.dto.request.RoleRequestDTO;
import com.healthcare.backend.dto.response.RoleResponseDTO;
import com.healthcare.backend.entity.Role;
import com.healthcare.backend.repository.RolePermissionRepository;
import com.healthcare.backend.repository.RoleRepository;

@Service
public class RoleServiceImpl implements RoleServiceInterface {
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Override
    public Page<RoleResponseDTO> getAllRole(Pageable pageable) {
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
        Role temp = new Role();

        temp.setRoleName(roleRequestDTO.getRoleName());
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

        existingRole.setRoleName(roleRequestDTO.getRoleName());
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

        boolean isUsing = rolePermissionRepository.existsByRole_RoleId(id);
        if (isUsing) {
            throw new RuntimeException("Cannot delete role: This role is currently assigned and has existing permissions.");
        }

        roleRepository.deleteById(id);
        return;
    }
}
