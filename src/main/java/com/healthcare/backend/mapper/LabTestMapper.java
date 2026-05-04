package com.healthcare.backend.mapper;

import com.healthcare.backend.dto.request.LabTestRequest;
import com.healthcare.backend.dto.response.LabTestResponse;
import com.healthcare.backend.entity.LabTest;
import org.springframework.stereotype.Component;

@Component
public class LabTestMapper {

    public LabTest toEntity(LabTestRequest request) {
        if (request == null) {
            return null;
        }

        LabTest labTest = new LabTest();
        labTest.setLabTestName(normalize(request.getLabTestName()));
        labTest.setPrice(request.getPrice());
        labTest.setIsActive(1);

        return labTest;
    }

    public void updateEntityFromRequest(LabTestRequest request, LabTest labTest) {
        if (request == null || labTest == null) {
            return;
        }

        labTest.setLabTestName(normalize(request.getLabTestName()));
        labTest.setPrice(request.getPrice());
    }

    public LabTestResponse toResponse(LabTest labTest) {
        if (labTest == null) {
            return null;
        }

        LabTestResponse response = new LabTestResponse();
        response.setLabTestId(labTest.getLabTestId());
        response.setLabTestName(labTest.getLabTestName());
        response.setPrice(labTest.getPrice());
        response.setActive(Integer.valueOf(1).equals(labTest.getIsActive()));

        return response;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}