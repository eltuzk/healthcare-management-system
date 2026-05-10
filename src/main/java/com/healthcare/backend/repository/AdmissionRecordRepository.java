package com.healthcare.backend.repository;

import com.healthcare.backend.entity.AdmissionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdmissionRecordRepository extends JpaRepository<AdmissionRecord, Long> {

    List<AdmissionRecord> findAllByAdmissionRequest_AdmissionId(Long admissionId);
}