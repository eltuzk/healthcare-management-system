package com.healthcare.backend.mapper;

import com.healthcare.backend.dto.response.LabTestRequestItemResponse;
import com.healthcare.backend.dto.response.LabTestRequestResponse;
import com.healthcare.backend.entity.LabTestRequest;
import com.healthcare.backend.entity.LabTestRequestItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LabTestRequestMapper {

    public LabTestRequestResponse toResponse(LabTestRequest entity) {
        if (entity == null) {
            return null;
        }

        List<LabTestRequestItemResponse> itemResponses = null;
        if (entity.getItems() != null) {
            itemResponses = entity.getItems().stream()
                    .map(this::toItemResponse)
                    .collect(Collectors.toList());
        }

        return LabTestRequestResponse.builder()
                .labTestRequestId(entity.getLabTestRequestId())
                .medRecordId(entity.getMedRecord() != null ? entity.getMedRecord().getMedicalRecordId() : null)
                .requestCode(entity.getRequestCode())
                .status(entity.getStatus())
                .paymentStatus(entity.getPaymentStatus())
                .totalPrice(entity.getTotalPrice())
                .note(entity.getNote())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .paidAt(entity.getPaidAt())
                .patientName(entity.getMedRecord() != null && entity.getMedRecord().getPatient() != null ? entity.getMedRecord().getPatient().getFullName() : null)
                .items(itemResponses)
                .build();
    }

    public LabTestRequestItemResponse toItemResponse(LabTestRequestItem item) {
        if (item == null) {
            return null;
        }

        return LabTestRequestItemResponse.builder()
                .labTestId(item.getLabTest() != null ? item.getLabTest().getLabTestId() : null)
                .labTestName(item.getLabTest() != null ? item.getLabTest().getLabTestName() : null)
                .snapshotPrice(item.getSnapshotPrice())
                .build();
    }
}
