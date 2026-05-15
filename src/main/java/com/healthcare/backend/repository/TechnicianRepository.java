package com.healthcare.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.healthcare.backend.entity.Technician;

import java.util.Optional;

@Repository
public interface TechnicianRepository extends JpaRepository<Technician, Long> {
    Optional<Technician> findByAccount_AccountId(Long accountId);
    Optional<Technician> findByAccount_Email(String email);
    boolean existsByIdentityNumAndTechnicianIdNot(String identityNum, Long id);
}
