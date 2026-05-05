package com.healthcare.backend.repository;

import com.healthcare.backend.entity.MedicalRecord;
import com.healthcare.backend.entity.enums.MedicalRecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    boolean existsByAppointment_AppointmentId(Long appointmentId);

    Optional<MedicalRecord> findByAppointment_AppointmentId(Long appointmentId);

    @Query("""
            select mr
            from MedicalRecord mr
            where (:patientId is null or mr.patient.patientId = :patientId)
              and (:doctorId is null or mr.doctor.doctorId = :doctorId)
              and (:status is null or mr.status = :status)
              and (:fromDate is null or mr.createdAt >= :fromDate)
              and (:toDate is null or mr.createdAt < :toDate)
            """)
    List<MedicalRecord> findAllByFilters(
            @Param("patientId") Long patientId,
            @Param("doctorId") Long doctorId,
            @Param("status") MedicalRecordStatus status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );
}
