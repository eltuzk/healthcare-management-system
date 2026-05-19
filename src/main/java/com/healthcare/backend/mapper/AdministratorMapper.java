package com.healthcare.backend.mapper;

import com.healthcare.backend.dto.request.AdministratorRequest;
import com.healthcare.backend.dto.response.AdministratorResponse;
import com.healthcare.backend.entity.Administrator;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class AdministratorMapper {

    public AdministratorResponse toResponse(Administrator admin) {
        if (admin == null) return null;

        AdministratorResponse response = new AdministratorResponse();
        response.setAdministratorId(admin.getAdministratorId());
        response.setFullName(admin.getFullName());
        response.setIdentityNum(admin.getIdentityNum());
        response.setGender(admin.getGender());
        response.setPhone(admin.getPhone());
        response.setAddress(admin.getAddress());
        response.setDateOfBirth(admin.getDateOfBirth());
        response.setHireDate(admin.getHireDate());
        response.setIsActive(admin.getIsActive());

        if (admin.getAccount() != null) {
            response.setAccountId(admin.getAccount().getAccountId());
            response.setEmail(admin.getAccount().getEmail());
        }

        return response;
    }

    public Administrator toEntity(AdministratorRequest request) {
        if (request == null) return null;

        Administrator admin = new Administrator();
        admin.setFullName(request.getFullName());
        admin.setIdentityNum(request.getIdentityNum());
        admin.setGender(normalizeGender(request.getGender()));
        admin.setPhone(request.getPhone());
        admin.setAddress(request.getAddress());
        admin.setDateOfBirth(request.getDateOfBirth());
        admin.setHireDate(request.getHireDate());
        admin.setIsActive(1);

        return admin;
    }

    public void updateEntityFromRequest(AdministratorRequest request, Administrator admin) {
        if (request == null || admin == null) return;

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            admin.setFullName(request.getFullName());
        }
        if (request.getIdentityNum() != null) {
            admin.setIdentityNum(request.getIdentityNum());
        }
        if (request.getGender() != null) {
            admin.setGender(normalizeGender(request.getGender()));
        }
        if (request.getPhone() != null) {
            admin.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            admin.setAddress(request.getAddress());
        }
        if (request.getDateOfBirth() != null) {
            admin.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getHireDate() != null) {
            admin.setHireDate(request.getHireDate());
        }
    }

    private String normalizeGender(String gender) {
        return gender == null || gender.isBlank()
                ? gender
                : gender.trim().toUpperCase(Locale.ROOT);
    }
}
