package com.healthcare.backend.service.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.healthcare.backend.dto.request.AuthRequest;
import com.healthcare.backend.dto.request.ForgotPasswordRequest;
import com.healthcare.backend.dto.request.RegisterRequest;
import com.healthcare.backend.dto.request.ResetPasswordRequest;
import com.healthcare.backend.dto.response.AuthResponse;
import com.healthcare.backend.dto.response.RegisterResponse;
import com.healthcare.backend.entity.Account;
import com.healthcare.backend.entity.Role;
import com.healthcare.backend.repository.AccountRepository;
import com.healthcare.backend.repository.RoleRepository;
import com.healthcare.backend.security.JwtServiceInterface;
import com.healthcare.backend.service.AuthService;
import com.healthcare.backend.service.EmailService;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;
    @Autowired
    private JwtServiceInterface jwtService;

    @Override
    public RegisterResponse register(RegisterRequest registerRequest) {
        if (accountRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email is existed.");
        }

        Account newAccount = new Account();
        newAccount.setEmail(registerRequest.getEmail());
        newAccount.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));

        Role userRole = roleRepository.findByRoleName("PATIENT")
            .orElseThrow(() -> new RuntimeException("No role existed."));
        newAccount.setRole(userRole);

        newAccount.setActive(false);

        accountRepository.save(newAccount);

        emailService.sendVerificationEmail(
            newAccount.getEmail(), 
            jwtService.generateVerificationToken(newAccount.getEmail())
        );
        
        return new RegisterResponse(registerRequest.getEmail());
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
    public AuthResponse login(AuthRequest authRequest) {
        Account account = accountRepository.findByEmail(authRequest.getEmail())
            .orElseThrow(() -> new RuntimeException("Email or password is incorrect."));

        if(!account.isActive()) {
            throw new RuntimeException("Account is not active.");
        }

        if (!passwordEncoder.matches(authRequest.getPassword(), account.getPasswordHash())) {
            throw new RuntimeException("Email or password is incorrect.");
        }

        String accessToken = jwtService.generateToken(account.getAccountId(), account.getEmail(), account.getRole().getRoleName());

        return new AuthResponse(accessToken);
    }

    @Override
    public void processForgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        String email = forgotPasswordRequest.getEmail();

        if(!accountRepository.existsByEmail(email)) {
            throw new RuntimeException("Email not existed.");
        }
        
        String verifyToken = jwtService.generateVerificationToken(email);
        emailService.sendForgotPasswordEmail(email, verifyToken);
    }

    @Override
    public void executeResetPassword(String token, ResetPasswordRequest resetPasswordRequest) {
        if(!jwtService.validateToken(token)) {
            throw new RuntimeException("Invalid or expired token.");
        }

        String email = jwtService.extractEmail(token);
        Account account = accountRepository.findByEmail(email).orElse(null);
        if(account == null) {
            throw new RuntimeException("Account not existed.");
        }

        if(!resetPasswordRequest.getNewPassword().equals(resetPasswordRequest.getConfirmNewPassword())) {
            throw new RuntimeException("New passwords do not match.");
        }
        if(passwordEncoder.matches(resetPasswordRequest.getNewPassword(), account.getPasswordHash())) {
            throw new RuntimeException("New password cannot be the same as the old password.");
        }

        account.setPasswordHash(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
        accountRepository.save(account);
    }
}
