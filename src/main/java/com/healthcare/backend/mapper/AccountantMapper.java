package com.healthcare.backend.mapper;

import com.healthcare.backend.dto.request.AccountantRequest;
import com.healthcare.backend.dto.response.AccountantResponse;
import com.healthcare.backend.entity.Account;
import com.healthcare.backend.entity.Accountant;
import org.springframework.stereotype.Component;

@Component
public class AccountantMapper {

    public Accountant toEntity(AccountantRequest request) {
        Accountant entity = new Accountant();
        
        if (request.getAccountId() != null) {
            Account account = new Account();
            account.setAccountId(request.getAccountId());
            entity.setAccount(account);
        }
        
        entity.setFullName(request.getFullName());
        entity.setQualification(request.getQualification());
        entity.setIdentityNum(request.getIdentityNum());
        entity.setGender(request.getGender());
        entity.setPhone(request.getPhone());
        entity.setAddress(request.getAddress());
        entity.setDateOfBirth(request.getDateOfBirth());
        entity.setHireDate(request.getHireDate());
        entity.setExperience(request.getExperience());
        entity.setIsActive(1); // Mặc định khi tạo mới
        
        return entity;
    }

    public AccountantResponse toResponse(Accountant entity) {
        AccountantResponse response = new AccountantResponse();
        
        response.setAccountantId(entity.getAccountantId());
        if (entity.getAccount() != null) {
            response.setAccountId(entity.getAccount().getAccountId());
        }
        
        response.setFullName(entity.getFullName());
        response.setQualification(entity.getQualification());
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

    public void updateEntityFromRequest(AccountantRequest request, Accountant entity) {
        if (request.getFullName() != null) entity.setFullName(request.getFullName());
        if (request.getQualification() != null) entity.setQualification(request.getQualification());
        if (request.getIdentityNum() != null) entity.setIdentityNum(request.getIdentityNum());
        if (request.getGender() != null) entity.setGender(request.getGender());
        if (request.getPhone() != null) entity.setPhone(request.getPhone());
        if (request.getAddress() != null) entity.setAddress(request.getAddress());
        if (request.getDateOfBirth() != null) entity.setDateOfBirth(request.getDateOfBirth());
        if (request.getHireDate() != null) entity.setHireDate(request.getHireDate());
        if (request.getExperience() != null) entity.setExperience(request.getExperience());
    }
}