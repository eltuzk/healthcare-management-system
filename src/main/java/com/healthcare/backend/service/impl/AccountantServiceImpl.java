package com.healthcare.backend.service.impl;

import com.healthcare.backend.dto.request.AccountantRequest;
import com.healthcare.backend.dto.response.AccountantResponse;
import com.healthcare.backend.entity.Accountant;
import com.healthcare.backend.mapper.AccountantMapper;
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
    public AccountantResponse update(Long id, AccountantRequest request) {
        Accountant existingAccountant = accountantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Accountant not found with id: " + id));

        accountantMapper.updateEntityFromRequest(request, existingAccountant);
        Accountant updatedAccountant = accountantRepository.save(existingAccountant);
        
        return accountantMapper.toResponse(updatedAccountant);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountantResponse getMe(String email) {
        return accountantMapper.toResponse(accountantRepository.findByAccount_Email(email)
                .orElseThrow(() -> new RuntimeException("Accountant not found with email: " + email)));
    }

    @Override
    @Transactional
    public AccountantResponse updateMe(String email, AccountantRequest request) {
        Accountant accountant = accountantRepository.findByAccount_Email(email)
                .orElseThrow(() -> new RuntimeException("Accountant not found with email: " + email));

        if (request.getIdentityNum() != null 
                && !request.getIdentityNum().isBlank()
                && accountantRepository.existsByIdentityNumAndAccountantIdNot(request.getIdentityNum(), accountant.getAccountantId())) {
            throw new RuntimeException("Số CCCD đã tồn tại: " + request.getIdentityNum());
        }

        accountantMapper.updateEntityFromRequest(request, accountant);
        return accountantMapper.toResponse(accountantRepository.save(accountant));
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