package com.healthcare.backend.repository;

import com.healthcare.backend.entity.Patient;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Page<Patient> findAllByIsActive(Integer isActive, Pageable pageable);

    Optional<Patient> findByAccount_AccountId(Long accountId);

    boolean existsByAccount_AccountId(Long accountId);

    boolean existsByIdentityNum(String identityNum);

    boolean existsByIdentityNumAndPatientIdNot(String identityNum, Long patientId);

    // Khóa bệnh nhân khi tạo appointment để tránh nhiều request cùng tạo lịch active cho một bệnh nhân.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select p
            from Patient p
            where p.patientId = :patientId
            """)
    Optional<Patient> findByIdForUpdate(@Param("patientId") Long patientId);
}
