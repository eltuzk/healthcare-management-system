package com.healthcare.backend.repository;

import com.healthcare.backend.entity.PaymentRecord;
import com.healthcare.backend.entity.enums.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {

    Optional<PaymentRecord> findByAppointment_AppointmentId(Long appointmentId);

    // Khóa payment record để việc ghi nhận tiền và transaction luôn nhất quán với nhau.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select pr
            from PaymentRecord pr
            where pr.appointment.appointmentId = :appointmentId
            """)
    Optional<PaymentRecord> findByAppointmentIdForUpdate(@Param("appointmentId") Long appointmentId);

    @Query("""
            select pr
            from PaymentRecord pr
            left join pr.appointment appointment
            left join pr.medicalRecord medicalRecord
            where (:paymentStatus is null or pr.paymentStatus = :paymentStatus)
              and (:appointmentId is null or appointment.appointmentId = :appointmentId)
              and (:medicalRecordId is null or medicalRecord.medicalRecordId = :medicalRecordId)
            """)
    List<PaymentRecord> findAllByFilters(
            @Param("paymentStatus") PaymentStatus paymentStatus,
            @Param("appointmentId") Long appointmentId,
            @Param("medicalRecordId") Long medicalRecordId
    );
}
