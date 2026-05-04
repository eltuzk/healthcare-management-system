package com.healthcare.backend.repository;

import com.healthcare.backend.entity.MedicalService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalServiceRepository extends JpaRepository<MedicalService, Long> {

    List<MedicalService> findAllByIsActiveOrderByMedicalServiceNameAsc(Integer isActive);

    Optional<MedicalService> findByMedServiceIdAndIsActive(Long medServiceId, Integer isActive);

    boolean existsByMedicalServiceNameIgnoreCase(String medicalServiceName);

    boolean existsByMedicalServiceNameIgnoreCaseAndMedServiceIdNot(
            String medicalServiceName,
            Long medServiceId
    );
}