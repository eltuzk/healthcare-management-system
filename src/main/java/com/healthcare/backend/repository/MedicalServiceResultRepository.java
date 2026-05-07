package com.healthcare.backend.repository;

import com.healthcare.backend.entity.MedicalServiceResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicalServiceResultRepository extends JpaRepository<MedicalServiceResult, Long> {

    Optional<MedicalServiceResult> findByMedicalServiceRequest_MedServiceRequestId(Long medServiceRequestId);
}
