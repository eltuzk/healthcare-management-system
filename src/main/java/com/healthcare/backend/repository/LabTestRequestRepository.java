package com.healthcare.backend.repository;

import com.healthcare.backend.entity.LabTestRequest;
import com.healthcare.backend.entity.enums.LabTestRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LabTestRequestRepository extends JpaRepository<LabTestRequest, Long> {

    List<LabTestRequest> findByMedRecord_MedicalRecordId(Long medRecordId);

    Page<LabTestRequest> findByStatus(LabTestRequestStatus status, Pageable pageable);

    Page<LabTestRequest> findByMedRecord_MedicalRecordIdAndStatus(Long medRecordId, LabTestRequestStatus status, Pageable pageable);
    
    Page<LabTestRequest> findByMedRecord_MedicalRecordId(Long medRecordId, Pageable pageable);
}
