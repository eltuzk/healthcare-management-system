package com.healthcare.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.healthcare.backend.entity.Doctor;

import jakarta.annotation.Nullable;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    @Query("SELECT d FROM doctor d WHERE (:specialization IS NULL OR d.specialization = :specialization)")
    Page<Doctor> findDoctorsBySpecialization(Pageable pageable, @Nullable String specialization);

    boolean existsByAccount_Email(String email);

    boolean existsByLicenseNum(String licenseNum);

    boolean existsByIdentifyNum(String identifyNum);
}
