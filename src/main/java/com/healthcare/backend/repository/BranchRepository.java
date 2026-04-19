package com.healthcare.backend.repository;
import com.healthcare.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface BranchRepository extends JpaRepository<Branch,Integer>{}
