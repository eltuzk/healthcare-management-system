package com.healthcare.backend.repository;

import com.healthcare.backend.entity.PatientInsurance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientInsuranceRepository extends JpaRepository<PatientInsurance, Long> {

    List<PatientInsurance> findAllByPatient_PatientId(Long patientId);

    boolean existsByInsuranceNum(String insuranceNum);

    boolean existsByInsuranceNumAndPatientInsuranceIdNot(String insuranceNum, Long patientInsuranceId);

    boolean existsByPatient_PatientIdAndStatus(Long patientId, String status);
}
