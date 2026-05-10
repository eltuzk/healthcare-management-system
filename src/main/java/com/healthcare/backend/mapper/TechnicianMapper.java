package com.healthcare.backend.mapper;

import com.healthcare.backend.dto.request.TechnicianRequest;
import com.healthcare.backend.dto.response.TechnicianResponse;
import com.healthcare.backend.entity.Account;
import com.healthcare.backend.entity.Technician;
import org.springframework.stereotype.Component;

@Component
public class TechnicianMapper {

    public Technician toEntity(TechnicianRequest request) {
        Technician entity = new Technician();
        
        if (request.getAccountId() != null) {
            Account account = new Account();
            account.setAccountId(request.getAccountId());
            entity.setAccount(account);
        }
        
        entity.setFullName(request.getFullName());
        entity.setQualification(request.getQualification());
        entity.setSpecialtyArea(request.getSpecialtyArea());
        entity.setLicenseNum(request.getLicenseNum());
        entity.setIdentityNum(request.getIdentityNum());
        entity.setGender(request.getGender());
        entity.setPhone(request.getPhone());
        entity.setAddress(request.getAddress());
        entity.setDateOfBirth(request.getDateOfBirth());
        entity.setHireDate(request.getHireDate());
        entity.setExperience(request.getExperience());
        entity.setIsActive(1);
        
        return entity;
    }

    public TechnicianResponse toResponse(Technician entity) {
        TechnicianResponse response = new TechnicianResponse();
        
        response.setTechnicianId(entity.getTechnicianId());
        if (entity.getAccount() != null) {
            response.setAccountId(entity.getAccount().getAccountId());
        }
        
        response.setFullName(entity.getFullName());
        response.setQualification(entity.getQualification());
        response.setSpecialtyArea(entity.getSpecialtyArea());
        response.setLicenseNum(entity.getLicenseNum());
        response.setIdentityNum(entity.getIdentityNum());
        response.setGender(entity.getGender());
        response.setPhone(entity.getPhone());
        response.setAddress(entity.getAddress());
        response.setDateOfBirth(entity.getDateOfBirth());
        response.setHireDate(entity.getHireDate());
        response.setExperience(entity.getExperience());
        response.setIsActive(entity.getIsActive());
        
        return response;
    }

    public void updateEntityFromRequest(TechnicianRequest request, Technician entity) {
        if (request.getFullName() != null) entity.setFullName(request.getFullName());
        if (request.getQualification() != null) entity.setQualification(request.getQualification());
        if (request.getSpecialtyArea() != null) entity.setSpecialtyArea(request.getSpecialtyArea());
        if (request.getLicenseNum() != null) entity.setLicenseNum(request.getLicenseNum());
        if (request.getIdentityNum() != null) entity.setIdentityNum(request.getIdentityNum());
        if (request.getGender() != null) entity.setGender(request.getGender());
        if (request.getPhone() != null) entity.setPhone(request.getPhone());
        if (request.getAddress() != null) entity.setAddress(request.getAddress());
        if (request.getDateOfBirth() != null) entity.setDateOfBirth(request.getDateOfBirth());
        if (request.getHireDate() != null) entity.setHireDate(request.getHireDate());
        if (request.getExperience() != null) entity.setExperience(request.getExperience());
    }
}