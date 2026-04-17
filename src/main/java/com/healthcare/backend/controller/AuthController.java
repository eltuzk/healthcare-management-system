package com.healthcare.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.healthcare.backend.dto.request.AuthRequestDTO;
import com.healthcare.backend.dto.request.ForgotPassword_EmailRequestDTO;
import com.healthcare.backend.dto.request.RegisterRequestDTO;
import com.healthcare.backend.dto.request.ResetPasswordRequestDTO;
import com.healthcare.backend.dto.response.AuthResponseDTO;
import com.healthcare.backend.dto.response.RegisterResponseDTO;
import com.healthcare.backend.service.AuthServiceInterface;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthServiceInterface authServiceInterface;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@Valid @RequestBody RegisterRequestDTO registerRequestDTO) {
        RegisterResponseDTO res = authServiceInterface.register(registerRequestDTO);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/register/verification-email")
    public ResponseEntity<String> verifyEmail(@Valid @RequestParam("token") String token) {
        try {
            authServiceInterface.verifyEmail(token);
            return ResponseEntity.ok("Verify successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("The verification link is invalid or has expired.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody AuthRequestDTO authRequestDTO) {
        AuthResponseDTO res = authServiceInterface.login(authRequestDTO);
        return ResponseEntity.ok(res.getAccessToken());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPassword_EmailRequestDTO forgotPasswordRequest) {
        authServiceInterface.processForgotPassword(forgotPasswordRequest);
        return ResponseEntity.ok("Mail sent successfully. Please check your email.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
        @RequestParam("token") String token, 
        @Valid @RequestBody ResetPasswordRequestDTO resetPasswordRequest
    ) {
        authServiceInterface.executeResetPassword(token, resetPasswordRequest);
        return ResponseEntity.ok("Password reset successfully.");
    }
}
