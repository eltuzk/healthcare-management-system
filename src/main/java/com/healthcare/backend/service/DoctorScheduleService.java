package com.healthcare.backend.service;

import com.healthcare.backend.dto.request.CreateDoctorScheduleRequest;
import com.healthcare.backend.dto.request.UpdateDoctorScheduleRequest;
import com.healthcare.backend.dto.response.DoctorScheduleImportResponse;
import com.healthcare.backend.dto.response.DoctorScheduleResponse;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface DoctorScheduleService {

    DoctorScheduleResponse create(CreateDoctorScheduleRequest request);

    DoctorScheduleImportResponse importSchedules(MultipartFile file);

    List<DoctorScheduleResponse> getAll(LocalDate date, Long doctorId, Long roomId);

    DoctorScheduleResponse getById(Long doctorScheduleId);

    DoctorScheduleResponse update(Long doctorScheduleId, UpdateDoctorScheduleRequest request);

    void delete(Long doctorScheduleId);
}
