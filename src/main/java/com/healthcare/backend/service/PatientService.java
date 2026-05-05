package com.healthcare.backend.service;

import com.healthcare.backend.dto.request.PatientRequest;
import com.healthcare.backend.dto.response.PatientResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PatientService {

    Page<PatientResponse> getAll(Pageable pageable);

    PatientResponse getById(Long id);

    PatientResponse create(PatientRequest request);

    PatientResponse update(Long id, PatientRequest request);

    void delete(Long id);

    PatientResponse getMe(String email);
}
