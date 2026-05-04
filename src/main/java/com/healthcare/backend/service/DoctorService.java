package com.healthcare.backend.service;

import com.healthcare.backend.dto.request.DoctorRequest;
import com.healthcare.backend.dto.response.DoctorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DoctorService {

    Page<DoctorResponse> getAll(Pageable pageable);

    DoctorResponse getById(Long doctorId);

    DoctorResponse create(DoctorRequest request);

    DoctorResponse update(Long doctorId, DoctorRequest request);

    void delete(Long doctorId);
}
