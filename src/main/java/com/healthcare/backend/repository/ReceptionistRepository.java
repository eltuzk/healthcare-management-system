package com.healthcare.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.healthcare.backend.entity.Receptionist;

@Repository
public interface ReceptionistRepository extends JpaRepository<Receptionist, Long> {

}
