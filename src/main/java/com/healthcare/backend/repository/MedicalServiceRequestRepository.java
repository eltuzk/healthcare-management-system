package com.healthcare.backend.repository;

import com.healthcare.backend.entity.MedicalServiceRequest;
import com.healthcare.backend.entity.enums.MedicalServiceRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalServiceRequestRepository extends JpaRepository<MedicalServiceRequest, Long> {

    List<MedicalServiceRequest> findByMedRecord_MedicalRecordId(Long medicalRecordId);
    Page<MedicalServiceRequest> findByMedRecord_MedicalRecordId(Long medicalRecordId, Pageable pageable);

    Page<MedicalServiceRequest> findByStatus(MedicalServiceRequestStatus status, Pageable pageable);

    Page<MedicalServiceRequest> findByMedRecord_MedicalRecordIdAndStatus(Long medicalRecordId, MedicalServiceRequestStatus status, Pageable pageable);
}
