package com.healthcare.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientRequest {

    private Long accountId;

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    private String gender;

    private LocalDate dateOfBirth;

    private String phone;

    private String address;

    private String identityNum;

    private String medicalHistory;

    private String allergy;
}
