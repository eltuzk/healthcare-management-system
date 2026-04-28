package com.healthcare.backend.mapper;

import com.healthcare.backend.dto.request.MedicalServiceRequest;
import com.healthcare.backend.dto.response.MedicalServiceResponse;
import com.healthcare.backend.entity.MedicalService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface MedicalServiceMapper {

    @Mapping(target = "medServiceId", ignore = true)
    @Mapping(target = "isActive", constant = "1")
    @Mapping(target = "medicalServiceName", source = "medicalServiceName", qualifiedByName = "normalize")
    MedicalService toEntity(MedicalServiceRequest request);

    @Mapping(target = "active", expression = "java(Integer.valueOf(1).equals(medicalService.getIsActive()))")
    MedicalServiceResponse toResponse(MedicalService medicalService);

    @Mapping(target = "medServiceId", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "medicalServiceName", source = "medicalServiceName", qualifiedByName = "normalize")
    void updateEntityFromRequest(
            MedicalServiceRequest request,
            @MappingTarget MedicalService medicalService
    );

    @Named("normalize")
    static String normalize(String value) {
        return value == null ? null : value.trim();
    }
}