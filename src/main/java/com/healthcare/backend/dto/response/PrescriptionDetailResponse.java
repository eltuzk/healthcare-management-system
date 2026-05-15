package com.healthcare.backend.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrescriptionDetailResponse {

    private Long prescriptionDetailId;

    private Long medicineId;

    private String medicineName;

    private String dosage;

    private String frequency;

    private String duration;

    private Integer quantity;

    private String instruction;
}