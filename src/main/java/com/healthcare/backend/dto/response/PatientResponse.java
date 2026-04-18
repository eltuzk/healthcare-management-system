package com.healthcare.backend.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatientResponse {
    private Long patientId;
    private String accountEmail;
    private String fullName;
    private String gender;
    private String dateOfBirth;
    private String phone;
    private String address;
    private String identityNum;
    private String medicalHistory;
    private String allergy;
    private Boolean isActive;
}
