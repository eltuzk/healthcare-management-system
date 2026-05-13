package com.healthcare.backend.repository;

import com.healthcare.backend.entity.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdministratorRepository extends JpaRepository<Administrator, Long> {
    Optional<Administrator> findByAccount_Email(String email);
    boolean existsByAccount_AccountId(Long accountId);
    boolean existsByIdentityNum(String identityNum);
    boolean existsByIdentityNumAndAdministratorIdNot(String identityNum, Long administratorId);
}
