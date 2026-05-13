package com.healthcare.backend.mapper;

import com.healthcare.backend.dto.request.ReceptionistRequest;
import com.healthcare.backend.dto.response.ReceptionistResponse;
import com.healthcare.backend.entity.Account;
import com.healthcare.backend.entity.Receptionist;
import org.springframework.stereotype.Component;

@Component
public class ReceptionistMapper {

    public Receptionist toEntity(ReceptionistRequest request) {
        Receptionist entity = new Receptionist();
        
        if (request.getAccountId() != null) {
            Account account = new Account();
            account.setAccountId(request.getAccountId());
            entity.setAccount(account);
        }
        
        entity.setFullName(request.getFullName());
        entity.setIdentityNum(request.getIdentityNum());
        entity.setGender(request.getGender());
        entity.setPhone(request.getPhone());
        entity.setAddress(request.getAddress());
        entity.setDateOfBirth(request.getDateOfBirth());
        entity.setHireDate(request.getHireDate());
        entity.setShift(request.getShift());
        entity.setIsActive(1);
        
        return entity;
    }

    public ReceptionistResponse toResponse(Receptionist entity) {
        ReceptionistResponse response = new ReceptionistResponse();
        
        response.setReceptionistId(entity.getReceptionistId());
        if (entity.getAccount() != null) {
            response.setAccountId(entity.getAccount().getAccountId());
        }
        
        response.setFullName(entity.getFullName());
        response.setIdentityNum(entity.getIdentityNum());
        response.setGender(entity.getGender());
        response.setPhone(entity.getPhone());
        response.setAddress(entity.getAddress());
        response.setDateOfBirth(entity.getDateOfBirth());
        response.setHireDate(entity.getHireDate());
        response.setShift(entity.getShift());
        response.setIsActive(entity.getIsActive());
        
        return response;
    }

    public void updateEntityFromRequest(ReceptionistRequest request, Receptionist entity) {
        if (request.getFullName() != null) entity.setFullName(request.getFullName());
        if (request.getIdentityNum() != null) entity.setIdentityNum(request.getIdentityNum());
        if (request.getGender() != null) entity.setGender(request.getGender());
        if (request.getPhone() != null) entity.setPhone(request.getPhone());
        if (request.getAddress() != null) entity.setAddress(request.getAddress());
        if (request.getDateOfBirth() != null) entity.setDateOfBirth(request.getDateOfBirth());
        if (request.getHireDate() != null) entity.setHireDate(request.getHireDate());
        if (request.getShift() != null) entity.setShift(request.getShift());
    }
}