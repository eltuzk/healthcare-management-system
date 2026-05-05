package com.healthcare.backend.service.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthcare.backend.dto.request.AccountPermissionRequest;
import com.healthcare.backend.dto.response.AccountPermissionResponse;
import com.healthcare.backend.entity.Account;
import com.healthcare.backend.entity.AccountPermission;
import com.healthcare.backend.entity.AccountPermissionId;
import com.healthcare.backend.entity.Permission;
import com.healthcare.backend.entity.RolePermissionId;
import com.healthcare.backend.exception.DuplicateResourceException;
import com.healthcare.backend.exception.ResourceNotFoundException;
import com.healthcare.backend.mapper.AccountPermissionMapper;
import com.healthcare.backend.repository.AccountPermissionRepository;
import com.healthcare.backend.repository.AccountRepository;
import com.healthcare.backend.repository.PermissionRepository;
import com.healthcare.backend.repository.RolePermissionRepository;
import com.healthcare.backend.service.AccountPermissionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountPermissionServiceImpl implements AccountPermissionService {

    private final AccountPermissionRepository accountPermissionRepository;
    private final AccountRepository accountRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final AccountPermissionMapper accountPermissionMapper;

    @Override
    public AccountPermissionResponse assign(AccountPermissionRequest request) {
        Account account = accountRepository.findById(request.getAccountId())
                .filter(a -> Objects.equals(a.getIsActive(), 1))
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + request.getAccountId()));

        Permission permission = permissionRepository.findById(request.getPermissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + request.getPermissionId()));

        AccountPermissionId id = new AccountPermissionId(request.getAccountId(), request.getPermissionId());

        if (accountPermissionRepository.existsById(id))
            throw new DuplicateResourceException("This permission is already assigned to this account");

        RolePermissionId rolePermissionId = new RolePermissionId();
        rolePermissionId.setRoleId(account.getRole().getRoleId());
        rolePermissionId.setPermissionId(request.getPermissionId());
        if (rolePermissionRepository.existsById(rolePermissionId))
            throw new DuplicateResourceException("Account's role already has this permission");

        AccountPermission entity = new AccountPermission();
        entity.setAccountPermissionId(id);
        entity.setAccount(account);
        entity.setPermission(permission);

        return accountPermissionMapper.toResponse(accountPermissionRepository.save(entity));
    }

    @Override
    public void revoke(AccountPermissionRequest request) {
        AccountPermissionId id = new AccountPermissionId(request.getAccountId(), request.getPermissionId());

        if (!accountPermissionRepository.existsById(id))
            throw new ResourceNotFoundException("This permission is not assigned to this account");

        accountPermissionRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountPermissionResponse> getByAccountId(Long accountId) {
        accountRepository.findById(accountId)
                .filter(a -> Objects.equals(a.getIsActive(), 1))
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));

        return accountPermissionRepository.findAllByAccount_AccountId(accountId).stream()
                .map(accountPermissionMapper::toResponse)
                .toList();
    }
}
