package com.healthcare.backend.mapper;

import com.healthcare.backend.dto.request.LabTestRequest;
import com.healthcare.backend.dto.response.LabTestResponse;
import com.healthcare.backend.entity.LabTest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface LabTestMapper {

    @Mapping(target = "labTestId", ignore = true)
    @Mapping(target = "isActive", constant = "1")
    @Mapping(target = "labTestName", source = "labTestName", qualifiedByName = "normalize")
    LabTest toEntity(LabTestRequest request);

    @Mapping(target = "active", expression = "java(Integer.valueOf(1).equals(labTest.getIsActive()))")
    LabTestResponse toResponse(LabTest labTest);

    @Mapping(target = "labTestId", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "labTestName", source = "labTestName", qualifiedByName = "normalize")
    void updateEntityFromRequest(LabTestRequest request, @MappingTarget LabTest labTest);

    @Named("normalize")
    static String normalize(String value) {
        return value == null ? null : value.trim();
    }
}