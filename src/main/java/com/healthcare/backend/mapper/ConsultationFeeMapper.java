package com.healthcare.backend.mapper;

import com.healthcare.backend.dto.request.ConsultationFeeRequest;
import com.healthcare.backend.dto.response.ConsultationFeeResponse;
import com.healthcare.backend.entity.ConsultationFee;
import org.springframework.stereotype.Component;

@Component
public class ConsultationFeeMapper {

    public ConsultationFee toEntity(ConsultationFeeRequest request) {
        if (request == null) {
            return null;
        }

        ConsultationFee consultationFee = new ConsultationFee();
        consultationFee.setFeeCode(normalize(request.getFeeCode()));
        consultationFee.setFeeName(normalize(request.getFeeName()));
        consultationFee.setPrice(request.getPrice());
        consultationFee.setIsActive(1);
        return consultationFee;
    }

    public void updateEntityFromRequest(ConsultationFeeRequest request, ConsultationFee consultationFee) {
        if (request == null || consultationFee == null) {
            return;
        }

        consultationFee.setFeeCode(normalize(request.getFeeCode()));
        consultationFee.setFeeName(normalize(request.getFeeName()));
        consultationFee.setPrice(request.getPrice());
    }

    public ConsultationFeeResponse toResponse(ConsultationFee consultationFee) {
        if (consultationFee == null) {
            return null;
        }

        ConsultationFeeResponse response = new ConsultationFeeResponse();
        response.setFeeId(consultationFee.getFeeId());
        response.setFeeCode(consultationFee.getFeeCode());
        response.setFeeName(consultationFee.getFeeName());
        response.setSpecialty(consultationFee.getSpecialtyRef() != null
                ? consultationFee.getSpecialtyRef().getSpecialtyName()
                : consultationFee.getSpecialty());
        if (consultationFee.getSpecialtyRef() != null) {
            response.setSpecialtyId(consultationFee.getSpecialtyRef().getSpecialtyId());
            response.setSpecialtyCode(consultationFee.getSpecialtyRef().getSpecialtyCode());
            response.setSpecialtyName(consultationFee.getSpecialtyRef().getSpecialtyName());
        }
        response.setPrice(consultationFee.getPrice());
        response.setActive(consultationFee.isActive());
        response.setCreatedAt(consultationFee.getCreatedAt());
        response.setUpdatedAt(consultationFee.getUpdatedAt());
        return response;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
