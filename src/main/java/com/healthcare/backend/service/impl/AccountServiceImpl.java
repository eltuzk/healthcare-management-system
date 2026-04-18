package com.healthcare.backend.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.healthcare.backend.dto.request.AccountRequest;
import com.healthcare.backend.dto.request.ChangePasswordRequest;
import com.healthcare.backend.dto.response.AccountResponse;
import com.healthcare.backend.dto.response.PermissionResponse;
import com.healthcare.backend.entity.Account;
import com.healthcare.backend.entity.AccountPermission;
import com.healthcare.backend.entity.AccountPermissionId;
import com.healthcare.backend.entity.Permission;
import com.healthcare.backend.entity.Role;
import com.healthcare.backend.entity.RolePermissionId;
import com.healthcare.backend.mapper.AccountMapper;
import com.healthcare.backend.mapper.PermissionMapper;
import com.healthcare.backend.repository.AccountPermissionRepository;
import com.healthcare.backend.repository.AccountRepository;
import com.healthcare.backend.repository.PermissionRepository;
import com.healthcare.backend.repository.RolePermissionRepository;
import com.healthcare.backend.repository.RoleRepository;
import com.healthcare.backend.service.AccountService;

@Service
public class AccountServiceImpl implements AccountService {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private AccountPermissionRepository accountPermissionRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private PermissionMapper permissionMapper;

    @Override
    public Page<AccountResponse> getAllAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable).map(accountMapper::toAccountResponse);
    }

    @Override
    public AccountResponse getAccountById(Long id) {
        return accountRepository.findById(id)
                .map(accountMapper::toAccountResponse)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));
    }

    @Override
    public AccountResponse createAccount(AccountRequest accountRequest) {
        String email = accountRequest.getEmail();
        if (accountRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists: " + email);
        }

        if (accountRequest.getRole().equalsIgnoreCase("PATIENT")) {
            throw new RuntimeException("Patient accounts must be created via the/register API.");
        }
        Account account = new Account();
        account.setEmail(accountRequest.getEmail());
        account.setPasswordHash(passwordEncoder.encode(accountRequest.getPassword()));

        Role role = roleRepository.findByRoleName(accountRequest.getRole().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Role not found: " + accountRequest.getRole()));
        account.setRole(role);

        account.setActive(true);
        accountRepository.save(account);

        return accountMapper.toAccountResponse(account);
    }

    @Override
    public AccountResponse updateAccount(Long id, AccountRequest accountRequest) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));

        if (accountRequest.getEmail() != null && !accountRequest.getEmail().equals(account.getEmail())) {
            throw new RuntimeException("Email cannot be updated.");
        }

        if (accountRequest.getRole() != null &&
                !accountRequest.getRole()
                        .toUpperCase()
                        .equalsIgnoreCase(account.getRole().getRoleName())) {
            Role role = roleRepository.findByRoleName(accountRequest.getRole().toUpperCase())
                    .orElseThrow(() -> new RuntimeException("Role not found: " + accountRequest.getRole()));
            account.setRole(role);
        }

        if (accountRequest.getPassword() != null) {
            account.setPasswordHash(passwordEncoder.encode(accountRequest.getPassword()));
        }

        if (accountRequest.isActive() != account.isActive()) {
            account.setActive(accountRequest.isActive());
        }

        accountRepository.save(account);

        return accountMapper.toAccountResponse(account);
    }

    @Override
    public void deleteAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));
        account.setActive(false);
        accountRepository.save(account);
    }

    @Override
    public void addPermissionToAccount(Long accountId, Long permissionId) {
        Account account = accountRepository.findById(accountId).orElse(null);
        if (account == null) {
            throw new RuntimeException("Account not found with id: " + accountId);
        }

        Permission permission = permissionRepository.findById(permissionId).orElse(null);
        if (permission == null) {
            throw new RuntimeException("Permission not found with id: " + permissionId);
        }

        AccountPermissionId accountPermissionId = new AccountPermissionId(accountId, permissionId);
        RolePermissionId rolePermissionId = new RolePermissionId(account.getRole().getRoleId(), permissionId);
        if (accountPermissionRepository.existsById(accountPermissionId)
                || rolePermissionRepository.existsById(rolePermissionId)) {
            throw new RuntimeException("This permission has already been assigned to this account.");
        }

        AccountPermission accountPermission = new AccountPermission(accountPermissionId, account, permission);
        accountPermissionRepository.save(accountPermission);
    }

    @Override
    public Map<String, Object> getPermissionsByAccount(Pageable pageable, Long accountId) {
        Account account = accountRepository.findById(accountId).orElse(null);
        if (account == null) {
            throw new RuntimeException("Account not found with id: " + accountId);
        }

        Long roleId = account.getRole().getRoleId();
        Page<PermissionResponse> permissionsByRole = rolePermissionRepository.findAllByRole_RoleId(roleId, pageable)
                .map(rolePermission -> permissionMapper.toPermissionResponse(rolePermission.getPermission()));

        Page<PermissionResponse> permissionsByAccount = accountPermissionRepository
                .findAllByAccount_AccountId(accountId, pageable)
                .map(accountPermission -> permissionMapper.toPermissionResponse(accountPermission.getPermission()));

        Map<String, Object> response = new HashMap<>();
        response.put("permissionsByRole", permissionsByRole);
        response.put("permissionsByAccount", permissionsByAccount);

        return response;
    }

    @Override
    public void changePassword(String email, ChangePasswordRequest changePasswordRequest) {
        Account account = accountRepository.findByEmail(email).orElse(null);
        if (account == null) {
            throw new RuntimeException();
        }

        if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), account.getPasswordHash())) {
            throw new RuntimeException("Old password incorrect.");
        }

        if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmNewPassword())) {
            throw new RuntimeException("New passwords do not match.");
        }

        if (passwordEncoder.matches(changePasswordRequest.getNewPassword(), account.getPasswordHash())) {
            throw new RuntimeException("New password cannot be the same as the old password.");
        }

        account.setPasswordHash(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        accountRepository.save(account);
    }
}
