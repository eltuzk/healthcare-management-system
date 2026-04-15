package com.healthcare.backend.service;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.healthcare.backend.dto.request.AccountRequestDTO;
import com.healthcare.backend.dto.response.AccountResponseDTO;

public interface AccountServiceInterface {
    Page<AccountResponseDTO> getAllAccounts (Pageable pageable);

    AccountResponseDTO getAccountById (Long id);

    AccountResponseDTO createAccount (AccountRequestDTO accountRequestDTO);

    AccountRequestDTO updateAccount (Long id, AccountRequestDTO accountRequestDTO);

    void deleteAccount (Long id);

    void addPermissionToAccount (Long accountId, Long permissionId);

    Map<String, Object> getPermissionsByAccount (Pageable pageable, Long accountId);
}
