package com.healthcare.backend.dto.response;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
public class TechnicianResponse {
    private Long technicianId;
    private Long accountId;
    private String fullName;
    private String qualification;
    private String specialtyArea;
    private String licenseNum;
    private String identityNum;
    private String gender;
    private String phone;
    private String address;
    private LocalDate dateOfBirth;
    private LocalDate hireDate;
    private Integer experience;
    private Integer isActive;
}