package com.healthcare.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.healthcare.backend.dto.request.DoctorRequest;
import com.healthcare.backend.dto.response.DoctorResponse;

import jakarta.annotation.Nullable;

public interface DoctorService {
    Page<DoctorResponse> getAllDoctors(Pageable pageable, @Nullable String specialization);

    DoctorResponse getDoctorById(Long doctorId);

    DoctorResponse createDoctor(DoctorRequest doctorRequest);

    DoctorResponse updateDoctor(DoctorRequest doctorRequest, Long doctorId);

    void deleteDoctor(Long doctorId);
}
