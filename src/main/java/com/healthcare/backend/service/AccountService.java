package com.healthcare.backend.service;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.healthcare.backend.dto.request.AccountRequest;
import com.healthcare.backend.dto.request.ChangePasswordRequest;
import com.healthcare.backend.dto.response.AccountResponse;

public interface AccountService {
    Page<AccountResponse> getAllAccounts (Pageable pageable);

    AccountResponse getAccountById (Long id);

    AccountResponse createAccount (AccountRequest accountRequest);

    AccountResponse updateAccount (Long id, AccountRequest accountRequest);

    void deleteAccount (Long id);

    void addPermissionToAccount (Long accountId, Long permissionId);

    Map<String, Object> getPermissionsByAccount (Pageable pageable, Long accountId);

    void changePassword(String email, ChangePasswordRequest changePasswordRequest);

}
