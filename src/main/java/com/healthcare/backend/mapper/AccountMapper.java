package com.healthcare.backend.mapper;

import org.springframework.stereotype.Component;

import com.healthcare.backend.dto.response.AccountResponse;
import com.healthcare.backend.entity.Account;

@Component
public class AccountMapper {
    public AccountResponse toAccountResponse(Account account) {
        if (account == null) {
            return null;
        }

        String roleName = account.getRole() != null ? account.getRole().getRoleName() : null;
        return new AccountResponse(account.getAccountId(), account.getEmail(), roleName, account.isActive());
    }
}
