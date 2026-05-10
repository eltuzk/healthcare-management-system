package com.healthcare.backend.service;

import com.healthcare.backend.dto.request.AccountantRequest;
import com.healthcare.backend.dto.response.AccountantResponse;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

public interface AccountantService {

    Page<AccountantResponse> getAll(Pageable pageable);

    AccountantResponse getById(Long id);

    AccountantResponse create(AccountantRequest request);

    AccountantResponse update(Long id, AccountantRequest request);

    void delete(Long id);
    
}