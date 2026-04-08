package com.healthcare.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.healthcare.backend.dto.request.RoleRequestDTO;
import com.healthcare.backend.dto.response.RoleResponseDTO;

public interface RoleServiceInterface {
    Page<RoleResponseDTO> getAllRole (Pageable pageable);

    RoleResponseDTO getRoleById (Long id);

    RoleResponseDTO createRole (RoleRequestDTO roleRequestDTO);

    RoleResponseDTO updateRole (Long id, RoleRequestDTO roleRequestDTO);

    void deleteRole (Long id);
}