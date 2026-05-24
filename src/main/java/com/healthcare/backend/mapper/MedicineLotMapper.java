package com.healthcare.backend.mapper;

import com.healthcare.backend.dto.request.MedicineLotRequest;
import com.healthcare.backend.dto.response.MedicineLotResponse;
import com.healthcare.backend.entity.Medicine;
import com.healthcare.backend.entity.MedicineLot;
import org.springframework.stereotype.Component;

@Component
public class MedicineLotMapper {

    public MedicineLot toEntity(MedicineLotRequest request, Medicine medicine) {
        if (request == null) {
            return null;
        }

        MedicineLot medicineLot = new MedicineLot();
        medicineLot.setMedicine(medicine);
        medicineLot.setLotNumber(normalize(request.getLotNumber()));
        medicineLot.setManufacturingDate(request.getManufacturingDate());
        medicineLot.setExpiryDate(request.getExpiryDate());
        medicineLot.setQuantity(request.getQuantity());
        medicineLot.setImportPrice(request.getImportPrice());
        medicineLot.setIsActive(1);
        if (request.getImportDate() != null) {
            medicineLot.setImportDate(request.getImportDate());
        }

        return medicineLot;
    }

    public void updateEntityFromRequest(MedicineLotRequest request, MedicineLot medicineLot, Medicine medicine) {
        if (request == null || medicineLot == null) {
            return;
        }

        medicineLot.setMedicine(medicine);
        medicineLot.setLotNumber(normalize(request.getLotNumber()));
        medicineLot.setManufacturingDate(request.getManufacturingDate());
        medicineLot.setExpiryDate(request.getExpiryDate());
        medicineLot.setQuantity(request.getQuantity());
        medicineLot.setImportPrice(request.getImportPrice());
        if (request.getImportDate() != null) {
            medicineLot.setImportDate(request.getImportDate());
        }
    }

    public MedicineLotResponse toResponse(MedicineLot medicineLot) {
        if (medicineLot == null) {
            return null;
        }

        MedicineLotResponse response = new MedicineLotResponse();
        response.setMedicineLotId(medicineLot.getMedicineLotId());

        if (medicineLot.getMedicine() != null) {
            response.setMedicineId(medicineLot.getMedicine().getMedicineId());
            response.setMedicineName(medicineLot.getMedicine().getMedicineName());
        }

        response.setLotNumber(medicineLot.getLotNumber());
        response.setManufacturingDate(medicineLot.getManufacturingDate());
        response.setExpiryDate(medicineLot.getExpiryDate());
        response.setQuantity(medicineLot.getQuantity());
        response.setImportPrice(medicineLot.getImportPrice());
        response.setActive(Integer.valueOf(1).equals(medicineLot.getIsActive()));
        response.setImportDate(medicineLot.getImportDate());

        return response;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}