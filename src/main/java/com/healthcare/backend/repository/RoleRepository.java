package com.healthcare.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.healthcare.backend.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>{
    boolean existsByRoleName(String roleName);
    
    Optional<Role> findByRoleName(String roleName);

    boolean existsByRoleNameAndRoleIdNot(String roleName, Long roleId);
}
