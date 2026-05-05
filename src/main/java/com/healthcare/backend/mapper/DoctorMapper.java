package com.healthcare.backend.mapper;

import com.healthcare.backend.dto.request.DoctorRequest;
import com.healthcare.backend.dto.response.DoctorResponse;
import com.healthcare.backend.entity.Doctor;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class DoctorMapper {

    public DoctorResponse toResponse(Doctor doctor) {
        if (doctor == null) return null;

        DoctorResponse response = new DoctorResponse();
        response.setDoctorId(doctor.getDoctorId());
        response.setFullName(doctor.getFullName());
        response.setQualification(doctor.getQualification());
        response.setSpecialization(doctor.getSpecialty() != null
                ? doctor.getSpecialty().getSpecialtyName()
                : doctor.getSpecialization());
        if (doctor.getSpecialty() != null) {
            response.setSpecialtyId(doctor.getSpecialty().getSpecialtyId());
            response.setSpecialtyCode(doctor.getSpecialty().getSpecialtyCode());
            response.setSpecialtyName(doctor.getSpecialty().getSpecialtyName());
        }
        response.setLicenseNum(doctor.getLicenseNum());
        response.setIdentityNum(doctor.getIdentityNum());
        response.setGender(doctor.getGender());
        response.setPhone(doctor.getPhone());
        response.setAddress(doctor.getAddress());
        response.setDateOfBirth(doctor.getDateOfBirth());
        response.setHireDate(doctor.getHireDate());
        response.setExperience(doctor.getExperience());
        response.setActive(doctor.isActive());

        if (doctor.getAccount() != null) {
            response.setAccountId(doctor.getAccount().getAccountId());
            response.setEmail(doctor.getAccount().getEmail());
        }

        return response;
    }

    public Doctor toEntity(DoctorRequest request) {
        if (request == null) return null;

        Doctor doctor = new Doctor();
        doctor.setFullName(request.getFullName());
        doctor.setQualification(request.getQualification());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setLicenseNum(request.getLicenseNum());
        doctor.setIdentityNum(request.getIdentityNum());
        doctor.setGender(normalizeGender(request.getGender()));
        doctor.setPhone(request.getPhone());
        doctor.setAddress(request.getAddress());
        doctor.setDateOfBirth(request.getDateOfBirth());
        doctor.setHireDate(request.getHireDate());
        doctor.setExperience(request.getExperience());

        return doctor;
    }

    public void updateEntityFromRequest(DoctorRequest request, Doctor doctor) {
        if (request == null || doctor == null) return;

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            doctor.setFullName(request.getFullName());
        }
        if (request.getLicenseNum() != null && !request.getLicenseNum().isBlank()) {
            doctor.setLicenseNum(request.getLicenseNum());
        }
        if (request.getIdentityNum() != null) {
            doctor.setIdentityNum(request.getIdentityNum());
        }
        if (request.getQualification() != null) {
            doctor.setQualification(request.getQualification());
        }
        if (request.getSpecialization() != null) {
            doctor.setSpecialization(request.getSpecialization());
        }
        if (request.getGender() != null) {
            doctor.setGender(normalizeGender(request.getGender()));
        }
        if (request.getPhone() != null) {
            doctor.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            doctor.setAddress(request.getAddress());
        }
        if (request.getDateOfBirth() != null) {
            doctor.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getHireDate() != null) {
            doctor.setHireDate(request.getHireDate());
        }
        if (request.getExperience() != null) {
            doctor.setExperience(request.getExperience());
        }
    }

    private String normalizeGender(String gender) {
        return gender == null || gender.isBlank()
                ? gender
                : gender.trim().toUpperCase(Locale.ROOT);
    }
}
