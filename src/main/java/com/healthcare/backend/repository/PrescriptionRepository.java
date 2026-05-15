package com.healthcare.backend.repository;

import com.healthcare.backend.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    List<Prescription> findAllByIsActiveOrderByCreatedAtDesc(Integer isActive);

    Optional<Prescription> findByPrescriptionIdAndIsActive(Long prescriptionId, Integer isActive);

    Optional<Prescription> findByMedicalRecord_MedicalRecordIdAndIsActive(Long medicalRecordId, Integer isActive);

    boolean existsByMedicalRecord_MedicalRecordIdAndIsActive(Long medicalRecordId, Integer isActive);
}