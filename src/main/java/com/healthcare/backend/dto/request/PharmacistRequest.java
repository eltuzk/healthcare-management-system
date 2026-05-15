package com.healthcare.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PharmacistRequest {

    private Long accountId;

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    private String qualification;

    @NotBlank(message = "Số chứng chỉ hành nghề không được để trống")
    private String licenseNum;

    @NotBlank(message = "Số CCCD không được để trống")
    private String identityNum;

    @Pattern(regexp = "(?i)MALE|FEMALE|OTHER", message = "Giới tính không hợp lệ")
    private String gender;

    private String phone;
    private String address;

    @Past(message = "Ngày sinh phải trong quá khứ")
    private LocalDate dateOfBirth;

    private LocalDate hireDate;

    @Min(value = 0, message = "Kinh nghiệm không được âm")
    private Integer experience;
}