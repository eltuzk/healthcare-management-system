package com.healthcare.backend.service.impl;

import java.util.Objects;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.healthcare.backend.dto.request.AuthRequest;
import com.healthcare.backend.dto.request.ChangePasswordRequest;
import com.healthcare.backend.dto.request.ForgotPasswordRequest;
import com.healthcare.backend.dto.request.RegisterRequest;
import com.healthcare.backend.dto.request.ResetPasswordRequest;
import com.healthcare.backend.dto.response.AuthResponse;
import com.healthcare.backend.dto.response.RegisterResponse;
import com.healthcare.backend.entity.Account;
import com.healthcare.backend.entity.Role;
import com.healthcare.backend.exception.BusinessException;
import com.healthcare.backend.exception.DuplicateResourceException;
import com.healthcare.backend.exception.ResourceNotFoundException;
import com.healthcare.backend.repository.AccountRepository;
import com.healthcare.backend.repository.RoleRepository;
import com.healthcare.backend.security.JwtServiceInterface;
import com.healthcare.backend.service.AuthService;
import com.healthcare.backend.service.EmailService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtServiceInterface jwtService;

    @Override
    public RegisterResponse register(RegisterRequest registerRequest) {
        if (accountRepository.existsByEmail(registerRequest.getEmail()))
            throw new DuplicateResourceException("Email already exists: " + registerRequest.getEmail());

        Role userRole = roleRepository.findByRoleName("PATIENT")
                .orElseThrow(() -> new ResourceNotFoundException("Role PATIENT not found"));

        Account newAccount = new Account();
        newAccount.setEmail(registerRequest.getEmail());
        newAccount.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        newAccount.setRole(userRole);
        newAccount.setIsActive(0);

        accountRepository.save(newAccount);

        emailService.sendVerificationEmail(
            newAccount.getEmail(),
            jwtService.generateVerificationToken(newAccount.getEmail())
        );

        RegisterResponse response = new RegisterResponse();
        response.setEmail(registerRequest.getEmail());
        return response;
    }

    @Override
    public void verifyEmail(String token) {
        if (!jwtService.validateToken(token))
            throw new BusinessException("Invalid or expired token");

        String email = jwtService.extractEmail(token);
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + email));

        if (Objects.equals(account.getIsActive(), 1))
            throw new BusinessException("Account is already active");

        account.setIsActive(1);
        accountRepository.save(account);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest authRequest) {
        Account account = accountRepository.findByEmail(authRequest.getEmail())
                .orElseThrow(() -> new BusinessException("Email or password is incorrect"));

        if (!Objects.equals(account.getIsActive(), 1))
            throw new BusinessException("Account is not active");

        if (!passwordEncoder.matches(authRequest.getPassword(), account.getPasswordHash()))
            throw new BusinessException("Email or password is incorrect");

        String accessToken = jwtService.generateToken(
            account.getAccountId(), account.getEmail(), account.getRole().getRoleName()
        );

        return new AuthResponse(accessToken);
    }

    @Override
    public void processForgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        String email = forgotPasswordRequest.getEmail();

        if (!accountRepository.existsByEmail(email))
            throw new ResourceNotFoundException("Email not found: " + email);

        String verifyToken = jwtService.generateVerificationToken(email);
        emailService.sendForgotPasswordEmail(email, verifyToken);
    }

    @Override
    public void executeResetPassword(String token, ResetPasswordRequest resetPasswordRequest) {
        if (!jwtService.validateToken(token))
            throw new BusinessException("Invalid or expired token");

        String email = jwtService.extractEmail(token);
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + email));

        if (!resetPasswordRequest.getNewPassword().equals(resetPasswordRequest.getConfirmNewPassword()))
            throw new BusinessException("New passwords do not match");

        if (passwordEncoder.matches(resetPasswordRequest.getNewPassword(), account.getPasswordHash()))
            throw new BusinessException("New password cannot be the same as the old password");

        account.setPasswordHash(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
        accountRepository.save(account);
    }

    @Override
    public void changePassword(String email, ChangePasswordRequest request) {
        Account account = accountRepository.findByEmailAndIsActive(email, 1)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + email));

        if (!passwordEncoder.matches(request.getOldPassword(), account.getPasswordHash()))
            throw new BusinessException("Old password incorrect");

        if (!request.getNewPassword().equals(request.getConfirmNewPassword()))
            throw new BusinessException("New passwords do not match");

        if (passwordEncoder.matches(request.getNewPassword(), account.getPasswordHash()))
            throw new BusinessException("New password cannot be the same as the old password");

        account.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);
    }
}
