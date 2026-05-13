package com.healthcare.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.healthcare.backend.entity.Pharmacist;

import java.util.Optional;

@Repository
public interface PharmacistRepository extends JpaRepository<Pharmacist, Long> {
    Optional<Pharmacist> findByAccount_AccountId(Long accountId);
    Optional<Pharmacist> findByAccount_Email(String email);
    boolean existsByIdentityNumAndPharmacistIdNot(String identityNum, Long id);
    boolean existsByLicenseNumAndPharmacistIdNot(String licenseNum, Long id);
}
