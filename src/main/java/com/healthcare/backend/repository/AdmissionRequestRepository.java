package com.healthcare.backend.repository;

import com.healthcare.backend.entity.AdmissionRequest;
import com.healthcare.backend.entity.enums.AdmissionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdmissionRequestRepository extends JpaRepository<AdmissionRequest, Long> {

    List<AdmissionRequest> findAllByPatient_PatientId(Long patientId);

    boolean existsByMedicalRecord_MedicalRecordId(Long medicalRecordId);

}