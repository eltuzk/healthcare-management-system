package com.healthcare.backend.mapper;

import com.healthcare.backend.dto.request.MedicineRequest;
import com.healthcare.backend.dto.response.MedicineResponse;
import com.healthcare.backend.entity.Medicine;
import org.springframework.stereotype.Component;

@Component
public class MedicineMapper {

    public Medicine toEntity(MedicineRequest request) {
        if (request == null) {
            return null;
        }

        Medicine medicine = new Medicine();
        medicine.setMedicineName(normalize(request.getMedicineName()));
        medicine.setActiveIngredient(normalize(request.getActiveIngredient()));
        medicine.setUnit(normalize(request.getUnit()));
        medicine.setDescription(normalize(request.getDescription()));
        medicine.setIsActive(1);

        return medicine;
    }

    public void updateEntityFromRequest(MedicineRequest request, Medicine medicine) {
        if (request == null || medicine == null) {
            return;
        }

        medicine.setMedicineName(normalize(request.getMedicineName()));
        medicine.setActiveIngredient(normalize(request.getActiveIngredient()));
        medicine.setUnit(normalize(request.getUnit()));
        medicine.setDescription(normalize(request.getDescription()));
    }

    public MedicineResponse toResponse(Medicine medicine) {
        if (medicine == null) {
            return null;
        }

        MedicineResponse response = new MedicineResponse();
        response.setMedicineId(medicine.getMedicineId());
        response.setMedicineName(medicine.getMedicineName());
        response.setActiveIngredient(medicine.getActiveIngredient());
        response.setUnit(medicine.getUnit());
        response.setDescription(medicine.getDescription());
        response.setActive(Integer.valueOf(1).equals(medicine.getIsActive()));

        return response;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}