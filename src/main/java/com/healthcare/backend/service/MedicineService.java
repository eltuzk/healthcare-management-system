package com.healthcare.backend.service;

import com.healthcare.backend.dto.request.MedicineRequest;
import com.healthcare.backend.dto.response.MedicineResponse;

import java.util.List;

public interface MedicineService {

    List<MedicineResponse> getAllMedicines();

    MedicineResponse getMedicineById(Long id);

    MedicineResponse createMedicine(MedicineRequest request);

    MedicineResponse updateMedicine(Long id, MedicineRequest request);

    MedicineResponse deactivateMedicine(Long id);
}