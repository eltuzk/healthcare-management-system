package com.healthcare.backend.repository;

import com.healthcare.backend.entity.PrescriptionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionDetailRepository extends JpaRepository<PrescriptionDetail, Long> {

    List<PrescriptionDetail> findAllByPrescription_PrescriptionId(Long prescriptionId);
}