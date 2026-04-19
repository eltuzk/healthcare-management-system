package com.healthcare.backend.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DoctorRequest {
    @NotBlank(message = "This field is required.")
    @Email(message = "Email invalid.")
    private String accountEmail;

    @NotBlank(message = "This field is required.")
    @Size(max = 100)
    private String fullName;

    @NotBlank(message = "This field is required.")
    private String specialization;

    @NotBlank(message = "This field is required.")
    private String licenseNum;

    @NotBlank(message = "This field is required.")
    private String qualification;

    @NotBlank(message = "This field is required.")
    private String experience;

    @NotBlank(message = "This field is required.")
    private String gender;

    @Pattern(regexp = "^(0|\\+84)(3|5|7|8|9)[0-9]{8}$", message = "Invalid phone number")
    @NotBlank(message = "This field is required.")
    private String phone;

    @NotBlank(message = "This field is required.")
    private String address;

    @PastOrPresent(message = "Hire date must be in the past or present")    
    private LocalDate hireDate;

    @NotBlank(message = "This field is required.")
    private String identityNum;

    @PastOrPresent(message = "Date of birth must be in the past or present")
    private LocalDate dateOfBirth;

    private boolean status;

}
