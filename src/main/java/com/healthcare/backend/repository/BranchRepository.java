package com.healthcare.backend.repository;

import com.healthcare.backend.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    boolean existsByBranchName(String branchName);

    boolean existsByBranchNameAndBranchIdNot(String branchName, Long branchId);
}
