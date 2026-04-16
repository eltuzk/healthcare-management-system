package com.healthcare.backend.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class DoctorRequestDTO {
    @NotNull(message = "This field is required.")
    private String accountEmail;

    @NotNull(message = "This field is required.")
    @Size(max = 100)
    private String fullName;

    private String specialization;

    private String licenseNum;

    private String qualification;

    private String experience;

    private String gender;

    @Pattern(regexp = "^(0|\\+84)(3|5|7|8|9)[0-9]{8}$", message = "Invalid phone number")
    private String phone;

    private String address;

    private LocalDate hireDate;

    private String identifyNum;

    private LocalDate dateOfBirth;

    private boolean status;

    public boolean isStatus() {
        return status;
    }
    public void setStatus(boolean status) {
        this.status = status;
    }
    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public String getSpecialization() {
        return specialization;
    }
    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
    public String getLicenseNum() {
        return licenseNum;
    }
    public void setLicenseNum(String licenseNum) {
        this.licenseNum = licenseNum;
    }
    public String getQualification() {
        return qualification;
    }
    public void setQualification(String qualification) {
        this.qualification = qualification;
    }
    public String getExperience() {
        return experience;
    }
    public void setExperience(String experience) {
        this.experience = experience;
    }
    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public LocalDate getHireDate() {
        return hireDate;
    }
    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }
    public String getIdentifyNum() {
        return identifyNum;
    }
    public void setIdentifyNum(String identifyNum) {
        this.identifyNum = identifyNum;
    }
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    public String getAccountEmail() {
        return accountEmail;
    }
    public void setAccountEmail(String accountEmail) {
        this.accountEmail = accountEmail;
    }

    
}
