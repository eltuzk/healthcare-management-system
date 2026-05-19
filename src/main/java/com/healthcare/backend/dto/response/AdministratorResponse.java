package com.healthcare.backend.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class AdministratorResponse {
    private Long administratorId;
    private Long accountId;
    private String email;
    private String fullName;
    private String identityNum;
    private String gender;
    private String phone;
    private String address;
    private LocalDate dateOfBirth;
    private LocalDate hireDate;
    private Integer isActive;
}
