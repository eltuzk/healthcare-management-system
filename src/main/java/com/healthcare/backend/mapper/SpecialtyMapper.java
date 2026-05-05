package com.healthcare.backend.mapper;

import com.healthcare.backend.dto.request.SpecialtyRequest;
import com.healthcare.backend.dto.response.SpecialtyResponse;
import com.healthcare.backend.entity.Specialty;
import org.springframework.stereotype.Component;

@Component
public class SpecialtyMapper {

    public Specialty toEntity(SpecialtyRequest request) {
        Specialty specialty = new Specialty();
        specialty.setSpecialtyCode(normalize(request.getSpecialtyCode()));
        specialty.setSpecialtyName(normalize(request.getSpecialtyName()));
        specialty.setIsActive(1);
        return specialty;
    }

    public void updateEntityFromRequest(SpecialtyRequest request, Specialty specialty) {
        specialty.setSpecialtyCode(normalize(request.getSpecialtyCode()));
        specialty.setSpecialtyName(normalize(request.getSpecialtyName()));
    }

    public SpecialtyResponse toResponse(Specialty specialty) {
        SpecialtyResponse response = new SpecialtyResponse();
        response.setSpecialtyId(specialty.getSpecialtyId());
        response.setSpecialtyCode(specialty.getSpecialtyCode());
        response.setSpecialtyName(specialty.getSpecialtyName());
        response.setActive(specialty.isActive());
        response.setCreatedAt(specialty.getCreatedAt());
        response.setUpdatedAt(specialty.getUpdatedAt());
        return response;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
