package com.healthcare.backend.dto.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientResponse {

    private Long patientId;
    private Long accountId;
    private String email;
    private String fullName;
    private String gender;
    private LocalDate dateOfBirth;
    private String phone;
    private String address;
    private String identityNum;
    private String medicalHistory;
    private String allergy;
    private Integer isActive;
}
