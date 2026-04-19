package com.healthcare.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.healthcare.backend.dto.request.AuthRequest;
import com.healthcare.backend.dto.request.ForgotPasswordRequest;
import com.healthcare.backend.dto.request.RegisterRequest;
import com.healthcare.backend.dto.request.ResetPasswordRequest;
import com.healthcare.backend.dto.response.AuthResponse;
import com.healthcare.backend.dto.response.RegisterResponse;
import com.healthcare.backend.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        RegisterResponse res = authService.register(registerRequest);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/register/verification-email")
    public ResponseEntity<String> verifyEmail(@Valid @RequestParam("token") String token) {
        try {
            authService.verifyEmail(token);
            return ResponseEntity.ok("Verify successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("The verification link is invalid or has expired.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody AuthRequest authRequest) {
        AuthResponse res = authService.login(authRequest);
        return ResponseEntity.ok(res.getAccessToken());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        authService.processForgotPassword(forgotPasswordRequest);
        return ResponseEntity.ok("Mail sent successfully. Please check your email.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
        @RequestParam("token") String token, 
        @Valid @RequestBody ResetPasswordRequest resetPasswordRequest
    ) {
        authService.executeResetPassword(token, resetPasswordRequest);
        return ResponseEntity.ok("Password reset successfully.");
    }
}
