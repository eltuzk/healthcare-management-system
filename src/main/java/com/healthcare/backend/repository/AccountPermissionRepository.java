package com.healthcare.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.healthcare.backend.entity.AccountPermission;
import com.healthcare.backend.entity.AccountPermissionId;

@Repository
public interface AccountPermissionRepository extends JpaRepository<AccountPermission, AccountPermissionId> {
    boolean existsByPermission_PermissionId(Long permissionId);

    List<AccountPermission> findAllByAccount_AccountId(Long accountId);
}
