package com.healthcare.backend.repository;

import com.healthcare.backend.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    List<Doctor> findAllByIsActive(boolean isActive);

    Page<Doctor> findAllByIsActive(boolean isActive, Pageable pageable);

    Optional<Doctor> findByAccount_AccountId(Long accountId);

    boolean existsByAccount_AccountId(Long accountId);

    boolean existsByLicenseNum(String licenseNum);

    boolean existsByLicenseNumAndDoctorIdNot(String licenseNum, Long doctorId);

    boolean existsByIdentityNum(String identityNum);

    boolean existsByIdentityNumAndDoctorIdNot(String identityNum, Long doctorId);
}
