package com.healthcare.backend.dto.response;

import java.time.LocalDate;

public class DoctorResponse {
    private Long doctorId;

    private String accountEmail;
    
    private String fullName;

    private String specialization;

    private String licenseNum;

    private String qualification;

    private String experience;

    private String gender;

    private String phone;

    private String address;

    private LocalDate hireDate;

    private String identityNum;
    
    private LocalDate dateOfBirth;
    
    private boolean status;

    public DoctorResponse() {
    }

    public DoctorResponse(Long doctorId, String accountEmail, String fullName, String specialization,
                          String licenseNum, String qualification, String experience, String gender, String phone, String address,
                          LocalDate hireDate, String identityNum, LocalDate dateOfBirth, boolean status) {
        this.doctorId = doctorId;
        this.accountEmail = accountEmail;
        this.fullName = fullName;
        this.specialization = specialization;
        this.licenseNum = licenseNum;
        this.qualification = qualification;
        this.experience = experience;
        this.gender = gender;
        this.phone = phone;
        this.address = address;
        this.hireDate = hireDate;
        this.identityNum = identityNum;
        this.dateOfBirth = dateOfBirth;
        this.status = status;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getAccountEmail() {
        return accountEmail;
    }

    public void setAccountEmail(String accountEmail) {
        this.accountEmail = accountEmail;
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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getIdentityNum() {
        return identityNum;
    }

    public void setIdentityNum(String identityNum) {
        this.identityNum = identityNum;
    }

    
}
