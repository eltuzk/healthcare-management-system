package com.healthcare.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.healthcare.backend.dto.request.DoctorRequestDTO;
import com.healthcare.backend.dto.response.DoctorResponseDTO;

import jakarta.annotation.Nullable;

public interface DoctorServiceInterface {    
    Page<DoctorResponseDTO> getAllDoctors(Pageable pageable, @Nullable String specialization);

    DoctorResponseDTO getDoctorById(Long doctorId);

    DoctorResponseDTO createDoctor(DoctorRequestDTO doctorRequest);

    DoctorResponseDTO updateDoctor(DoctorRequestDTO doctorRequest, Long doctorId);

    void deleteDoctor(Long doctorId);
}
