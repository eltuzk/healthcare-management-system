package com.healthcare.backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.healthcare.backend.dto.request.AuthRequestDTO;
import com.healthcare.backend.dto.request.ChangePasswordRequestDTO;
import com.healthcare.backend.dto.request.RegisterRequestDTO;
import com.healthcare.backend.dto.response.AuthResponseDTO;
import com.healthcare.backend.dto.response.RegisterResponseDTO;
import com.healthcare.backend.entity.Account;
import com.healthcare.backend.entity.Role;
import com.healthcare.backend.repository.AccountRepository;
import com.healthcare.backend.repository.RoleRepository;
import com.healthcare.backend.security.JwtServiceInterface;
import com.healthcare.backend.service.AuthServiceInterface;
import com.healthcare.backend.service.EmailServiceInterface;

@Service
public class AuthServiceImpl implements AuthServiceInterface {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailServiceInterface emailService;
    @Autowired
    private JwtServiceInterface jwtService;

    @Override
    public RegisterResponseDTO register(RegisterRequestDTO registerRequestDTO) {
        if (accountRepository.existsByEmail(registerRequestDTO.getEmail())) {
            throw new RuntimeException("Email is existed.");
        }

        Account newAccount = new Account();
        newAccount.setEmail(registerRequestDTO.getEmail());
        newAccount.setPasswordHash(passwordEncoder.encode(registerRequestDTO.getPassword()));

        Role userRole = roleRepository.findByRoleName("PATIENT")
            .orElseThrow(() -> new RuntimeException("No role existed."));
        newAccount.setRole(userRole);

        newAccount.setActive(false);

        accountRepository.save(newAccount);

        emailService.sendVerificationEmail(
            newAccount.getEmail(), 
            jwtService.generateVerificationToken(newAccount.getEmail())
        );
        
        return new RegisterResponseDTO(registerRequestDTO.getEmail());
    }

    @Override
    public void verifyEmail(String token) {
        String email = jwtService.extractEmail(token);
        Account account = accountRepository.findByEmail(email).orElse(null);
        if(account == null) {
            throw new RuntimeException("Account not existed.");
        }

        if (account.isActive()) {
            throw new RuntimeException("Account is already active.");
        }

        account.setActive(true);
        accountRepository.save(account);
    }

    @Override
    public AuthResponseDTO login(AuthRequestDTO authRequestDTO) {
        Account account = accountRepository.findByEmail(authRequestDTO.getEmail())
            .orElseThrow(() -> new RuntimeException("Email or password is incorrect."));

        if(!account.isActive()) {
            throw new RuntimeException("Account is not active.");
        }

        if (!passwordEncoder.matches(authRequestDTO.getPassword(), account.getPasswordHash())) {
            throw new RuntimeException("Email or password is incorrect.");
        }

        String accessToken = jwtService.generateToken(account.getAccountId(), account.getEmail(), account.getRole().getRoleName());

        return new AuthResponseDTO(accessToken);
    }

    @Override
    public void changePassword(String email, ChangePasswordRequestDTO changePasswordRequestDTO) {
        Account account = accountRepository.findByEmail(email).orElse(null);
        if(account == null) {
            throw new RuntimeException();
        }

        if(changePasswordRequestDTO.getOldPassword().matches(account.getPasswordHash())) {
            throw new RuntimeException("Old password incorrect.");
        }

        if(!changePasswordRequestDTO.getNewPassword().equals(changePasswordRequestDTO.getConfirmNewPassword())) {
            throw new RuntimeException("New passwords do not match.");
        }

        if(changePasswordRequestDTO.getNewPassword().matches(account.getPasswordHash())) {
            throw new RuntimeException("New password cannot be the same as the old password.");
        }

        account.setPasswordHash(passwordEncoder.encode(changePasswordRequestDTO.getNewPassword()));
        accountRepository.save(account);
    }

    @Override
    public void forgotPassword() {

    }
}
