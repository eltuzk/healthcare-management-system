package com.healthcare.backend.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.healthcare.backend.dto.request.AccountRequestDTO;
import com.healthcare.backend.dto.request.ChangePasswordRequestDTO;
import com.healthcare.backend.dto.response.AccountResponseDTO;
import com.healthcare.backend.dto.response.PermissionResponseDTO;
import com.healthcare.backend.entity.Account;
import com.healthcare.backend.entity.AccountPermission;
import com.healthcare.backend.entity.AccountPermissionId;
import com.healthcare.backend.entity.Permission;
import com.healthcare.backend.entity.Role;
import com.healthcare.backend.entity.RolePermissionId;
import com.healthcare.backend.repository.AccountPermissionRepository;
import com.healthcare.backend.repository.AccountRepository;
import com.healthcare.backend.repository.PermissionRepository;
import com.healthcare.backend.repository.RolePermissionRepository;
import com.healthcare.backend.repository.RoleRepository;
import com.healthcare.backend.service.AccountServiceInterface;

@Service
public class AccountServiceImpl implements AccountServiceInterface {
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

    @Override
    public Page<AccountResponseDTO> getAllAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable).map(account -> 
            new AccountResponseDTO(account.getAccountId(), account.getEmail(), account.getRole().getRoleName(), account.isActive()));
    }

    @Override
    public AccountResponseDTO getAccountById(Long id) {
        return accountRepository.findById(id)
            .map(account -> new AccountResponseDTO(account.getAccountId(), account.getEmail(), account.getRole().getRoleName(), account.isActive()))
            .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));
    }

    @Override
    public AccountResponseDTO createAccount(AccountRequestDTO accountRequestDTO) {
        String email = accountRequestDTO.getEmail();
        if (accountRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists: " + email);
        }

        if(accountRequestDTO.getRole().equalsIgnoreCase("PATIENT")) {
            throw new RuntimeException("Cannot create account with role PATIENT.");
        }

        Account account = new Account();
        account.setEmail(accountRequestDTO.getEmail());
        account.setPasswordHash(passwordEncoder.encode(accountRequestDTO.getPassword()));

        Role role = roleRepository.findByRoleName(accountRequestDTO.getRole().toUpperCase())
            .orElseThrow(() -> new RuntimeException("Role not found: " + accountRequestDTO.getRole()));
        account.setRole(role);

        account.setActive(true);
        accountRepository.save(account);

        return new AccountResponseDTO(account.getAccountId(), account.getEmail(), account.getRole().getRoleName(), account.isActive());
    }

    @Override
    public AccountResponseDTO updateAccount(Long id, AccountRequestDTO accountRequestDTO) {
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));

        if (accountRequestDTO.getEmail() != null && !accountRequestDTO.getEmail().equals(account.getEmail())) {
            throw new RuntimeException("Email cannot be updated.");
        }

        if (accountRequestDTO.getRole() != null && 
            !accountRequestDTO.getRole()
                    .toUpperCase()
                    .equalsIgnoreCase(account.getRole().getRoleName())
            ) {
            Role role = roleRepository.findByRoleName(accountRequestDTO.getRole().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Role not found: " + accountRequestDTO.getRole()));
            account.setRole(role);
        }

        if (accountRequestDTO.getPassword() != null) {
            account.setPasswordHash(passwordEncoder.encode(accountRequestDTO.getPassword()));
        }

        if (accountRequestDTO.isActive() != account.isActive()) {
            account.setActive(accountRequestDTO.isActive());
        }

        accountRepository.save(account);

        return new AccountResponseDTO(account.getAccountId(), account.getEmail(), account.getRole().getRoleName(), account.isActive());
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
        if (accountPermissionRepository.existsById(accountPermissionId) || rolePermissionRepository.existsById(rolePermissionId)) {
            throw new RuntimeException("This permission has already been assigned to this account.");
        }

        AccountPermission accountPermission = new AccountPermission(accountPermissionId, account, permission);
        accountPermissionRepository.save(accountPermission);
    }

    @Override
    public Map<String, Object> getPermissionsByAccount (Pageable pageable, Long accountId) {
        Account account = accountRepository.findById(accountId).orElse(null);
        if (account == null) {
            throw new RuntimeException("Account not found with id: " + accountId);
        }

        Long roleId = account.getRole().getRoleId();
        Page<PermissionResponseDTO> permissionsByRole = rolePermissionRepository.findAllByRole_RoleId(roleId, pageable)
            .map(rolePermission -> new PermissionResponseDTO(
                rolePermission.getPermission().getId(), 
                rolePermission.getPermission().getPermissionName(),
                rolePermission.getPermission().getDetail()
            ));

        Page<PermissionResponseDTO> permissionsByAccount = accountPermissionRepository.findAllByAccount_AccountId(accountId, pageable)
            .map(accountPermission -> new PermissionResponseDTO(
                accountPermission.getPermission().getId(),
                accountPermission.getPermission().getPermissionName(),
                accountPermission.getPermission().getDetail()
            ));

        Map<String, Object> response = new HashMap<>();
        response.put("permissionsByRole", permissionsByRole);
        response.put("permissionsByAccount", permissionsByAccount);

        return response;
    }

    @Override
    public void changePassword(String email, ChangePasswordRequestDTO changePasswordRequestDTO) {
        Account account = accountRepository.findByEmail(email).orElse(null);
        if(account == null) {
            throw new RuntimeException();
        }

        if(!passwordEncoder.matches(changePasswordRequestDTO.getOldPassword(), account.getPasswordHash())) {
            throw new RuntimeException("Old password incorrect.");
        }

        if(!changePasswordRequestDTO.getNewPassword().equals(changePasswordRequestDTO.getConfirmNewPassword())) {
            throw new RuntimeException("New passwords do not match.");
        }

        if(passwordEncoder.matches(changePasswordRequestDTO.getNewPassword(), account.getPasswordHash())) {
            throw new RuntimeException("New password cannot be the same as the old password.");
        }

        account.setPasswordHash(passwordEncoder.encode(changePasswordRequestDTO.getNewPassword()));
        accountRepository.save(account);
    }
}
