package com.healthcare.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.healthcare.backend.entity.RolePermission;
import com.healthcare.backend.entity.RolePermissionId;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {
    boolean existsByRole_RoleId(Long roleId);
    boolean existsByPermission_PermissionId(Long permissionId);

    Page<RolePermission> findAllById_RoleId(Long roleId, Pageable pageable);
}
