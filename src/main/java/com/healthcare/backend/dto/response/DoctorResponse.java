package com.healthcare.backend.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class DoctorResponse {

    private Long doctorId;

    private Long accountId;

    private String email;

    private String fullName;

    private String qualification;

    private String specialization;

    private Long specialtyId;

    private String specialtyCode;

    private String specialtyName;

    private String licenseNum;

    private String identityNum;

    private String gender;

    private String phone;

    private String address;

    private LocalDate dateOfBirth;

    private LocalDate hireDate;

    private String experience;

    private boolean isActive;
}
