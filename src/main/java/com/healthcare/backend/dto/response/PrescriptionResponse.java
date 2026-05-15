package com.healthcare.backend.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PrescriptionResponse {

    private Long prescriptionId;

    private Long medicalRecordId;

    private String note;

    private boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<PrescriptionDetailResponse> details;
}