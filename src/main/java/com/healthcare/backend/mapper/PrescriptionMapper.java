package com.healthcare.backend.mapper;

import com.healthcare.backend.dto.request.PrescriptionRequest;
import com.healthcare.backend.dto.response.PrescriptionResponse;
import com.healthcare.backend.entity.MedicalRecord;
import com.healthcare.backend.entity.PaymentRecord;
import com.healthcare.backend.entity.Prescription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PrescriptionMapper {

    private final PrescriptionDetailMapper prescriptionDetailMapper;

    public Prescription toEntity(PrescriptionRequest request, MedicalRecord medicalRecord) {
        if (request == null) {
            return null;
        }

        Prescription prescription = new Prescription();
        prescription.setMedicalRecord(medicalRecord);
        prescription.setNote(normalize(request.getNote()));
        prescription.setIsActive(1);

        return prescription;
    }

    public void updateEntityFromRequest(PrescriptionRequest request, Prescription prescription) {
        if (request == null || prescription == null) {
            return;
        }

        prescription.setNote(normalize(request.getNote()));
    }

    public PrescriptionResponse toResponse(Prescription prescription) {
        if (prescription == null) {
            return null;
        }

        PrescriptionResponse response = new PrescriptionResponse();
        response.setPrescriptionId(prescription.getPrescriptionId());

        if (prescription.getMedicalRecord() != null) {
            response.setMedicalRecordId(prescription.getMedicalRecord().getMedicalRecordId());
        }

        response.setNote(prescription.getNote());
        response.setActive(Integer.valueOf(1).equals(prescription.getIsActive()));
        response.setCreatedAt(prescription.getCreatedAt());
        response.setUpdatedAt(prescription.getUpdatedAt());

        response.setDetails(
                prescription.getPrescriptionDetails()
                        .stream()
                        .map(prescriptionDetailMapper::toResponse)
                        .toList()
        );

        if (prescription.getPaymentRecord() != null) {
            response.setPaymentStatus(prescription.getPaymentRecord().getPaymentStatus().name());
            response.setTotalPrice(prescription.getPaymentRecord().getTotalPrice());
        }

        return response;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}