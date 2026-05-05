package com.healthcare.backend.mapper;

import org.springframework.stereotype.Component;

import com.healthcare.backend.dto.response.AccountPermissionResponse;
import com.healthcare.backend.entity.AccountPermission;

@Component
public class AccountPermissionMapper {

    public AccountPermissionResponse toResponse(AccountPermission entity) {
        AccountPermissionResponse response = new AccountPermissionResponse();
        if (entity.getAccount() != null) {
            response.setAccountId(entity.getAccount().getAccountId());
            response.setEmail(entity.getAccount().getEmail());
        }
        if (entity.getPermission() != null) {
            response.setPermissionId(entity.getPermission().getPermissionId());
            response.setPermissionName(entity.getPermission().getPermissionName());
        }
        return response;
    }
}
