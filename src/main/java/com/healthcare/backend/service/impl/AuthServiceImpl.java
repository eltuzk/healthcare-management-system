package com.healthcare.backend.service.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.healthcare.backend.dto.request.AuthRequestDTO;
import com.healthcare.backend.dto.request.ForgotPassword_EmailRequestDTO;
import com.healthcare.backend.dto.request.RegisterRequestDTO;
import com.healthcare.backend.dto.request.ResetPasswordRequestDTO;
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
        if(!jwtService.validateToken(token)) {
            throw new RuntimeException("Invalid or expired token.");
        }

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

        // if (!authRequestDTO.getPassword().equals(account.getPasswordHash())) {
        //     throw new RuntimeException("Email or password is incorrect.");
        // }

        String accessToken = jwtService.generateToken(account.getAccountId(), account.getEmail(), account.getRole().getRoleName());

        return new AuthResponseDTO(accessToken);
    }

    @Override
    public void processForgotPassword(ForgotPassword_EmailRequestDTO forgotPasswordRequest) {
        String email = forgotPasswordRequest.getEmail();

        if(!accountRepository.existsByEmail(email)) {
            throw new RuntimeException("Email not existed.");
        }
        
        String verifyToken = jwtService.generateVerificationToken(email);
        emailService.sendForgotPasswordEmail(email, verifyToken);
    }

    @Override
    public void executeResetPassword(String token, ResetPasswordRequestDTO resetPassworsRequest) {
        if(!jwtService.validateToken(token)) {
            throw new RuntimeException("Invalid or expired token.");
        }

        String email = jwtService.extractEmail(token);
        Account account = accountRepository.findByEmail(email).orElse(null);
        if(account == null) {
            throw new RuntimeException("Account not existed.");
        }

        if(!resetPassworsRequest.getNewPassword().equals(resetPassworsRequest.getConfirmNewPassword())) {
            throw new RuntimeException("New passwords do not match.");
        }
        if(passwordEncoder.matches(resetPassworsRequest.getNewPassword(), account.getPasswordHash())) {
            throw new RuntimeException("New password cannot be the same as the old password.");
        }

        account.setPasswordHash(passwordEncoder.encode(resetPassworsRequest.getNewPassword()));
        accountRepository.save(account);
    }
}
