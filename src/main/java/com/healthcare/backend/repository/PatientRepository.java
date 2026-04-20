package com.healthcare.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.healthcare.backend.entity.Patient;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    boolean existsByIdentityNum(String identityNum);

    boolean existsByPhone(String phone);

    boolean existsByAccount_Email(String email);

    Optional<Patient> findByAccount_AccountId(Long accountId);
}
