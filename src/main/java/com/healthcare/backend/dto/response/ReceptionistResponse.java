package com.healthcare.backend.dto.response;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
public class ReceptionistResponse {
    private Long receptionistId;
    private Long accountId;
    private String fullName;
    private String identityNum;
    private String gender;
    private String phone;
    private String address;
    private LocalDate dateOfBirth;
    private LocalDate hireDate;
    private String shift;
    private Integer isActive;
}