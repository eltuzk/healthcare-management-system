package com.healthcare.backend.service;

import com.healthcare.backend.dto.request.PrescriptionRequest;
import com.healthcare.backend.dto.response.PrescriptionResponse;

import java.util.List;

public interface PrescriptionService {

    List<PrescriptionResponse> getAllPrescriptions();

    PrescriptionResponse getPrescriptionById(Long id);

    PrescriptionResponse getPrescriptionByMedicalRecordId(Long medicalRecordId);

    PrescriptionResponse createPrescription(PrescriptionRequest request);

    PrescriptionResponse updatePrescription(Long id, PrescriptionRequest request);

    PrescriptionResponse deactivatePrescription(Long id);

    PrescriptionResponse dispensePrescription(Long id);
}