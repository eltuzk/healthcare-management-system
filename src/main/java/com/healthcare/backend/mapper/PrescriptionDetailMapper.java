package com.healthcare.backend.mapper;

import com.healthcare.backend.dto.request.PrescriptionDetailRequest;
import com.healthcare.backend.dto.response.PrescriptionDetailResponse;
import com.healthcare.backend.entity.Medicine;
import com.healthcare.backend.entity.Prescription;
import com.healthcare.backend.entity.PrescriptionDetail;
import org.springframework.stereotype.Component;

@Component
public class PrescriptionDetailMapper {

    public PrescriptionDetail toEntity(
            PrescriptionDetailRequest request,
            Prescription prescription,
            Medicine medicine
    ) {
        if (request == null) {
            return null;
        }

        PrescriptionDetail detail = new PrescriptionDetail();
        detail.setPrescription(prescription);
        detail.setMedicine(medicine);
        detail.setDosage(normalize(request.getDosage()));
        detail.setFrequency(normalize(request.getFrequency()));
        detail.setDuration(normalize(request.getDuration()));
        detail.setQuantity(request.getQuantity());
        detail.setInstruction(normalize(request.getInstruction()));

        return detail;
    }

    public PrescriptionDetailResponse toResponse(PrescriptionDetail detail) {
        if (detail == null) {
            return null;
        }

        PrescriptionDetailResponse response = new PrescriptionDetailResponse();
        response.setPrescriptionDetailId(detail.getPrescriptionDetailId());

        if (detail.getMedicine() != null) {
            response.setMedicineId(detail.getMedicine().getMedicineId());
            response.setMedicineName(detail.getMedicine().getMedicineName());
        }

        response.setDosage(detail.getDosage());
        response.setFrequency(detail.getFrequency());
        response.setDuration(detail.getDuration());
        response.setQuantity(detail.getQuantity());
        response.setInstruction(detail.getInstruction());

        return response;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}