package com.healthcare.backend.repository;

import com.healthcare.backend.entity.LabTestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LabTestResultRepository extends JpaRepository<LabTestResult, Long> {

    Optional<LabTestResult> findByLabTestRequest_LabTestRequestId(Long labTestRequestId);
}
