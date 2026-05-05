package com.healthcare.backend.dto.request;

import com.healthcare.backend.entity.enums.ShiftType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateDoctorScheduleRequest {

    @NotNull(message = "Bác sĩ không được để trống")
    private Long doctorId;

    @NotNull(message = "Phòng không được để trống")
    private Long roomId;

    @NotNull(message = "Ngày làm việc không được để trống")
    @FutureOrPresent(message = "Ngày làm việc không được ở trong quá khứ")
    private LocalDate scheduleDate;

    @NotNull(message = "Ca làm việc không được để trống")
    private ShiftType shift;

    @NotNull(message = "Sức chứa tối đa không được để trống")
    @Positive(message = "Sức chứa tối đa phải lớn hơn 0")
    @Max(value = 99999, message = "Sức chứa tối đa không được vượt quá 99999")
    private Integer maxCapacity;

    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    private String note;
}
