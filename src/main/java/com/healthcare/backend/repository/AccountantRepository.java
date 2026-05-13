package com.healthcare.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.healthcare.backend.entity.Accountant;

import java.util.Optional;

@Repository
public interface AccountantRepository extends JpaRepository<Accountant, Long> {
    Optional<Accountant> findByAccount_AccountId(Long accountId);
    Optional<Accountant> findByAccount_Email(String email);
    boolean existsByIdentityNumAndAccountantIdNot(String identityNum, Long id);
}
