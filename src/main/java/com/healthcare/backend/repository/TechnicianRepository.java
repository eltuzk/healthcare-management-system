package com.healthcare.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.healthcare.backend.entity.Technician;

@Repository
public interface TechnicianRepository extends JpaRepository<Technician, Long> {

}
