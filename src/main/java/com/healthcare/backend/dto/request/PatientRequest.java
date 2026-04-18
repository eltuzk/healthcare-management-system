package com.healthcare.backend.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

@Data
public class PatientRequest {
    private String accountEmail;

    @NotBlank(message = "This field is required.")
    private String fullName;

    @NotBlank(message = "This field is required.")
    private String gender;

    @PastOrPresent(message = "Date of birth must be in the past or present")
    private LocalDate dateOfBirth;

    @NotBlank(message = "This field is required.")
    private String phone;

    @NotBlank(message = "This field is required.")
    private String address;

    @NotBlank(message = "This field is required.")
    private String identityNum;

    private String medicalHistory;

    private String allergy;

    private Boolean isActive;
}
