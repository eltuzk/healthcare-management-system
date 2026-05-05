package com.healthcare.backend.repository;

import com.healthcare.backend.entity.Appointment;
import com.healthcare.backend.entity.enums.AppointmentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findFirstByPatient_PatientIdAndStatusIn(Long patientId, Collection<AppointmentStatus> statuses);

    boolean existsByAppointmentCode(String appointmentCode);

    boolean existsBySepayTransactionId(Long sepayTransactionId);

    // Khóa bi quan appointment để chỉ một transaction được phép đổi trạng thái tại một thời điểm.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select a
            from Appointment a
            where a.appointmentId = :appointmentId
            """)
    Optional<Appointment> findByIdForUpdate(@Param("appointmentId") Long appointmentId);

    Optional<Appointment> findByAppointmentCode(String appointmentCode);

    // Khóa theo mã appointment khi SePay callback về, tránh hai webhook xử lý cùng một lịch hẹn song song.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select a
            from Appointment a
            where a.appointmentCode = :appointmentCode
            """)
    Optional<Appointment> findByAppointmentCodeForUpdate(@Param("appointmentCode") String appointmentCode);

    Optional<Appointment> findBySepayTransactionId(Long sepayTransactionId);

    // Scheduler lấy các reservation hết hạn kèm khóa ghi để tránh confirm payment và expire chạy đè nhau.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select a
            from Appointment a
            where a.status = :status
              and a.paymentExpiresAt is not null
              and a.paymentExpiresAt <= :now
            """)
    List<Appointment> findExpiredPaymentReservationsForUpdate(
            @Param("status") AppointmentStatus status,
            @Param("now") LocalDateTime now
    );

    @Query("""
            select a
            from Appointment a
            where (:patientId is null or a.patient.patientId = :patientId)
              and (:doctorScheduleId is null or a.doctorSchedule.doctorScheduleId = :doctorScheduleId)
              and (:status is null or a.status = :status)
            """)
    List<Appointment> findAllByFilters(
            @Param("patientId") Long patientId,
            @Param("doctorScheduleId") Long doctorScheduleId,
            @Param("status") AppointmentStatus status
    );
}
