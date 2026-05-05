package com.healthcare.backend.mapper;

import com.healthcare.backend.dto.response.LabTestResultResponse;
import com.healthcare.backend.entity.LabTestResult;
import org.springframework.stereotype.Component;

@Component
public class LabTestResultMapper {

    public LabTestResultResponse toResponse(LabTestResult entity) {
        if (entity == null) {
            return null;
        }

        return LabTestResultResponse.builder()
                .labTestResultId(entity.getLabTestResultId())
                .labTestRequestId(entity.getLabTestRequest() != null ? entity.getLabTestRequest().getLabTestRequestId() : null)
                .resultData(entity.getResultData())
                .resultDate(entity.getResultDate())
                .build();
    }
}
