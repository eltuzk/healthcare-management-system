package com.healthcare.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class DoctorRequest {

    @NotNull(message = "ID tài khoản không được để trống")
    private Long accountId;

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @NotBlank(message = "Số giấy phép không được để trống")
    private String licenseNum;

    private String identityNum;

    private String qualification;

    private String specialization;

    @NotNull(message = "ID chuyên khoa không được để trống")
    private Long specialtyId;

    @Pattern(regexp = "(?i)MALE|FEMALE|OTHER", message = "Giới tính chỉ được là MALE, FEMALE hoặc OTHER")
    private String gender;

    private String phone;

    private String address;

    @PastOrPresent(message = "Ngày sinh không được lớn hơn hiện tại")
    private LocalDate dateOfBirth;

    @PastOrPresent(message = "Ngày vào làm không được lớn hơn hiện tại")
    private LocalDate hireDate;

    private String experience;
}
