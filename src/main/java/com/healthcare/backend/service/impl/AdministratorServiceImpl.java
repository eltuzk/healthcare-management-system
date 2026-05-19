package com.healthcare.backend.service.impl;

import com.healthcare.backend.dto.request.AdministratorRequest;
import com.healthcare.backend.dto.response.AdministratorResponse;
import com.healthcare.backend.entity.Account;
import com.healthcare.backend.entity.Administrator;
import com.healthcare.backend.exception.DuplicateResourceException;
import com.healthcare.backend.exception.ResourceNotFoundException;
import com.healthcare.backend.mapper.AdministratorMapper;
import com.healthcare.backend.repository.AccountRepository;
import com.healthcare.backend.repository.AdministratorRepository;
import com.healthcare.backend.service.AdministratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdministratorServiceImpl implements AdministratorService {

    private final AdministratorRepository administratorRepository;
    private final AccountRepository accountRepository;
    private final AdministratorMapper administratorMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<AdministratorResponse> getAll(Pageable pageable) {
        return administratorRepository.findAll(pageable).map(administratorMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AdministratorResponse getById(Long adminId) {
        return administratorMapper.toResponse(findOrThrow(adminId));
    }

    @Override
    @Transactional(readOnly = true)
    public AdministratorResponse getMe(String email) {
        return administratorRepository.findByAccount_Email(email)
                .map(administratorMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản quản trị không tồn tại cho email: " + email));
    }

    @Override
    @Transactional
    public AdministratorResponse updateMe(String email, AdministratorRequest request) {
        Administrator admin = administratorRepository.findByAccount_Email(email)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản quản trị không tồn tại cho email: " + email));

        if (request.getIdentityNum() != null 
                && !request.getIdentityNum().isBlank()
                && administratorRepository.existsByIdentityNumAndAdministratorIdNot(request.getIdentityNum(), admin.getAdministratorId())) {
            throw new DuplicateResourceException("Số CCCD đã tồn tại: " + request.getIdentityNum());
        }

        administratorMapper.updateEntityFromRequest(request, admin);
        return administratorMapper.toResponse(administratorRepository.save(admin));
    }

    @Override
    @Transactional
    public AdministratorResponse create(AdministratorRequest request) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với id: " + request.getAccountId()));

        if (administratorRepository.existsByAccount_AccountId(request.getAccountId())) {
            throw new DuplicateResourceException("Tài khoản này đã có hồ sơ quản trị viên");
        }

        if (request.getIdentityNum() != null && administratorRepository.existsByIdentityNum(request.getIdentityNum())) {
            throw new DuplicateResourceException("Số CCCD đã tồn tại: " + request.getIdentityNum());
        }

        Administrator admin = administratorMapper.toEntity(request);
        admin.setAccount(account);
        return administratorMapper.toResponse(administratorRepository.save(admin));
    }

    @Override
    @Transactional
    public AdministratorResponse update(Long adminId, AdministratorRequest request) {
        Administrator admin = findOrThrow(adminId);

        if (request.getIdentityNum() != null
                && administratorRepository.existsByIdentityNumAndAdministratorIdNot(request.getIdentityNum(), adminId)) {
            throw new DuplicateResourceException("Số CCCD đã tồn tại: " + request.getIdentityNum());
        }

        administratorMapper.updateEntityFromRequest(request, admin);
        return administratorMapper.toResponse(administratorRepository.save(admin));
    }

    @Override
    @Transactional
    public void delete(Long adminId) {
        Administrator admin = findOrThrow(adminId);
        admin.setIsActive(0);
        administratorRepository.save(admin);
    }

    private Administrator findOrThrow(Long adminId) {
        return administratorRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quản trị viên với id: " + adminId));
    }
}
