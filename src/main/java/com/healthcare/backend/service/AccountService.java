package com.healthcare.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.healthcare.backend.dto.request.AccountRequest;
import com.healthcare.backend.dto.response.AccountResponse;

public interface AccountService {
    Page<AccountResponse> getAll(Pageable pageable);

    AccountResponse getById(Long id);

    AccountResponse create(AccountRequest request);

    AccountResponse update(Long id, AccountRequest request);

    void delete(Long id);

    AccountResponse getMe(String email);
}
