package com.healthcare.backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.healthcare.backend.entity.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByEmail(String email);

    Optional<Account> findByEmail(String email);

    Optional<Account> findByEmailAndIsActive(String email, Integer isActive);

    Page<Account> findAllByIsActive(Integer isActive, Pageable pageable);

    boolean existsByRole_RoleId(Long roleId);
}
