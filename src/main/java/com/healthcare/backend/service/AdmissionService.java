package com.healthcare.backend.service;

import com.healthcare.backend.dto.request.AdmissionRecordRequest;
import com.healthcare.backend.dto.request.AdmissionRequestRequest;
import com.healthcare.backend.dto.request.AdmissionStatusUpdateRequest;
import com.healthcare.backend.dto.response.AdmissionRecordResponse;
import com.healthcare.backend.dto.response.AdmissionRequestResponse;

import java.util.List;

public interface AdmissionService {

    // AdmissionRequest
    List<AdmissionRequestResponse> getAll();

    AdmissionRequestResponse getById(Long admissionId);

    List<AdmissionRequestResponse> getByPatientId(Long patientId);

    AdmissionRequestResponse create(AdmissionRequestRequest request);

    AdmissionRequestResponse updateStatus(Long admissionId, AdmissionStatusUpdateRequest request);

    // AdmissionRecord
    List<AdmissionRecordResponse> getRecords(Long admissionId);

    AdmissionRecordResponse createRecord(Long admissionId, AdmissionRecordRequest request);

    AdmissionRecordResponse updateRecord(Long recordId, AdmissionRecordRequest request);
}