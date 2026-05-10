package com.healthcare.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
public class ReceptionistRequest {

    @NotNull(message = "Account ID is required")
    private Long accountId;

    @NotBlank(message = "Full name is required")
    @Size(max = 200)
    private String fullName;

    @NotBlank(message = "Identity number is required")
    @Size(max = 50)
    private String identityNum;

    @Size(max = 10)
    private String gender;

    @Size(max = 20)
    private String phone;

    @Size(max = 500)
    private String address;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private LocalDate hireDate;

    @Size(max = 50)
    private String shift;
}