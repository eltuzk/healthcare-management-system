package com.healthcare.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.healthcare.backend.entity.Receptionist;

import java.util.Optional;

@Repository
public interface ReceptionistRepository extends JpaRepository<Receptionist, Long> {
    Optional<Receptionist> findByAccount_AccountId(Long accountId);
    Optional<Receptionist> findByAccount_Email(String email);
    boolean existsByIdentityNumAndReceptionistIdNot(String identityNum, Long id);
}
