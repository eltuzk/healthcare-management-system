package com.healthcare.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DoctorScheduleImportResponse {

    private int createdCount;
    private List<DoctorScheduleResponse> schedules;
}
