package com.healthcare.backend.repository;

import com.healthcare.backend.entity.LabTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LabTestRepository extends JpaRepository<LabTest, Long> {

    List<LabTest> findAllByIsActiveOrderByLabTestNameAsc(Integer isActive);

    Optional<LabTest> findByLabTestIdAndIsActive(Long labTestId, Integer isActive);

    boolean existsByLabTestNameIgnoreCase(String labTestName);

    boolean existsByLabTestNameIgnoreCaseAndLabTestIdNot(String labTestName, Long labTestId);
}