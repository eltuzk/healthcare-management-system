package com.healthcare.backend.repository;

import com.healthcare.backend.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Page<Patient> findAllByIsActive(Integer isActive, Pageable pageable);

    Optional<Patient> findByAccount_AccountId(Long accountId);

    boolean existsByAccount_AccountId(Long accountId);

    boolean existsByIdentityNum(String identityNum);

    boolean existsByIdentityNumAndPatientIdNot(String identityNum, Long patientId);
}
