package com.healthcare.backend.mapper;

import org.springframework.stereotype.Component;

import com.healthcare.backend.dto.request.DoctorRequestDTO;
import com.healthcare.backend.dto.response.DoctorResponseDTO;
import com.healthcare.backend.entity.Doctor;

@Component
public class DoctorMapper {
    public DoctorResponseDTO toDto(Doctor doctor) {
        if (doctor == null) {
            return null;
        }

        DoctorResponseDTO response = new DoctorResponseDTO();
        response.setDoctorId(doctor.getDoctorId());
        response.setFullName(doctor.getFullName());
        response.setSpecialization(doctor.getSpecialization());
        response.setLicenseNum(doctor.getLicenseNum());
        response.setQualification(doctor.getQualification());
        response.setExperience(doctor.getExperience());
        response.setGender(doctor.getGender());
        response.setPhone(doctor.getPhone());
        response.setAddress(doctor.getAddress());
        response.setHireDate(doctor.getHireDate());
        response.setIdentityNum(doctor.getIdentityNum());
        response.setDateOfBirth(doctor.getDateOfBirth());
        response.setStatus(doctor.isActive());

        if (doctor.getAccount() != null) {
            response.setAccountEmail(doctor.getAccount().getEmail());
        }
        
        return response;
    }

    public Doctor createEntityFromDto(DoctorRequestDTO requestDTO) {
        if (requestDTO == null) return null;

        Doctor doctor = new Doctor();
        
        doctor.setFullName(requestDTO.getFullName());
        doctor.setSpecialization(requestDTO.getSpecialization());
        doctor.setLicenseNum(requestDTO.getLicenseNum());
        doctor.setQualification(requestDTO.getQualification());
        doctor.setExperience(requestDTO.getExperience());
        doctor.setGender(requestDTO.getGender());
        doctor.setPhone(requestDTO.getPhone());
        doctor.setAddress(requestDTO.getAddress());
        doctor.setHireDate(requestDTO.getHireDate());
        doctor.setIdentityNum(requestDTO.getIdentityNum());
        doctor.setDateOfBirth(requestDTO.getDateOfBirth());
        doctor.setActive(requestDTO.isStatus());

        return doctor;
    }

    public void updateEntityFromDto(Doctor doctor, DoctorRequestDTO requestDTO) {
        if (requestDTO == null || doctor == null) return;

        doctor.setFullName(requestDTO.getFullName());
        doctor.setSpecialization(requestDTO.getSpecialization());
        doctor.setLicenseNum(requestDTO.getLicenseNum());
        doctor.setQualification(requestDTO.getQualification());
        doctor.setExperience(requestDTO.getExperience());
        doctor.setGender(requestDTO.getGender());
        doctor.setPhone(requestDTO.getPhone());
        doctor.setAddress(requestDTO.getAddress());
        doctor.setIdentityNum(requestDTO.getIdentityNum());
        doctor.setDateOfBirth(requestDTO.getDateOfBirth());
        doctor.setHireDate(requestDTO.getHireDate());
        doctor.setActive(requestDTO.isStatus());
    }

    
}
