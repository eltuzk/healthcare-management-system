package com.healthcare.backend.mapper;

import org.springframework.stereotype.Component;

import com.healthcare.backend.dto.request.AccountRequest;
import com.healthcare.backend.dto.response.AccountResponse;
import com.healthcare.backend.entity.Account;

@Component
public class AccountMapper {

    public Account toEntity(AccountRequest request) {
        Account entity = new Account();
        entity.setEmail(request.getEmail());
        return entity;
    }

    public AccountResponse toResponse(Account entity) {
        AccountResponse response = new AccountResponse();
        response.setAccountId(entity.getAccountId());
        response.setEmail(entity.getEmail());
        if (entity.getRole() != null) {
            response.setRoleId(entity.getRole().getRoleId());
            response.setRoleName(entity.getRole().getRoleName());
        }
        response.setIsActive(entity.getIsActive());
        return response;
    }

    public void updateEntityFromRequest(AccountRequest request, Account entity) {
        if (request.getEmail() != null) entity.setEmail(request.getEmail());
    }
}
