package com.healthcare.backend.repository;

import com.healthcare.backend.entity.DoctorSchedule;
import com.healthcare.backend.entity.enums.ShiftType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {

    boolean existsByDoctor_DoctorIdAndScheduleDateAndShift(Long doctorId, LocalDate scheduleDate, ShiftType shift);

    boolean existsByDoctor_DoctorIdAndScheduleDateAndShiftAndDoctorScheduleIdNot(
            Long doctorId,
            LocalDate scheduleDate,
            ShiftType shift,
            Long doctorScheduleId
    );

    boolean existsByRoom_RoomIdAndScheduleDateAndShift(Long roomId, LocalDate scheduleDate, ShiftType shift);

    boolean existsByRoom_RoomIdAndScheduleDateAndShiftAndDoctorScheduleIdNot(
            Long roomId,
            LocalDate scheduleDate,
            ShiftType shift,
            Long doctorScheduleId
    );

    @Query("""
            select ds
            from DoctorSchedule ds
            where (:scheduleDate is null or ds.scheduleDate = :scheduleDate)
              and (:doctorId is null or ds.doctor.doctorId = :doctorId)
              and (:roomId is null or ds.room.roomId = :roomId)
            """)
    List<DoctorSchedule> findAllByFilters(
            @Param("scheduleDate") LocalDate scheduleDate,
            @Param("doctorId") Long doctorId,
            @Param("roomId") Long roomId
    );

    // Khóa bi quan ca khám khi cấp số thứ tự hoặc release slot để không vượt quá sức chứa.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select ds
            from DoctorSchedule ds
            where ds.doctorScheduleId = :doctorScheduleId
            """)
    Optional<DoctorSchedule> findByIdForUpdate(@Param("doctorScheduleId") Long doctorScheduleId);
}
