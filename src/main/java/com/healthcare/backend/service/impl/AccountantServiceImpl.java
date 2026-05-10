package com.healthcare.backend.service.impl;

import com.healthcare.backend.dto.request.AccountantRequest;
import com.healthcare.backend.dto.response.AccountantResponse;
import com.healthcare.backend.entity.Account;
import com.healthcare.backend.entity.Accountant;
import com.healthcare.backend.mapper.AccountantMapper;
import com.healthcare.backend.repository.AccountRepository;
import com.healthcare.backend.repository.AccountantRepository;
import com.healthcare.backend.service.AccountantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountantServiceImpl implements AccountantService {

    private final AccountantRepository accountantRepository;
    private final AccountRepository accountRepository;
    private final AccountantMapper accountantMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<AccountantResponse> getAll(Pageable pageable) {
        return accountantRepository.findAll(pageable)
                .map(accountantMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountantResponse getById(Long id) {
        Accountant accountant = accountantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Accountant not found with id: " + id));
        return accountantMapper.toResponse(accountant);
    }

    @Override
    @Transactional
    public AccountantResponse create(AccountantRequest request) {
        Accountant accountant = accountantMapper.toEntity(request);

        if (request.getAccountId() != null) {
            Account account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Account not found with id: " + request.getAccountId()));
            accountant.setAccount(account);
        }

        Accountant savedAccountant = accountantRepository.save(accountant);
        return accountantMapper.toResponse(savedAccountant);
    }

    @Override
    @Transactional
    public AccountantResponse update(Long id, AccountantRequest request) {
        Accountant existingAccountant = accountantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Accountant not found with id: " + id));

        accountantMapper.updateEntityFromRequest(request, existingAccountant);
        Accountant updatedAccountant = accountantRepository.save(existingAccountant);
        
        return accountantMapper.toResponse(updatedAccountant);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Accountant accountant = accountantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Accountant not found with id: " + id));
        
        accountant.setIsActive(0); 
        accountantRepository.save(accountant);
    }
}