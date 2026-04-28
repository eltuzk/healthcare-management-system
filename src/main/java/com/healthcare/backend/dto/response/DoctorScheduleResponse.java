package com.healthcare.backend.dto.response;

import com.healthcare.backend.entity.enums.ShiftType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DoctorScheduleResponse {

    private Long doctorScheduleId;
    private Long doctorId;
    private String doctorName;
    private Long roomId;
    private String roomCode;
    private Long branchId;
    private String branchName;
    private LocalDate scheduleDate;
    private ShiftType shift;
    private String shiftStartTime;
    private String shiftEndTime;
    private Integer maxCapacity;
    private Integer currentBookingCount;
    private Integer lastQueueNumber;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
