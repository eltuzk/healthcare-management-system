package com.healthcare.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.healthcare.backend.dto.request.AuthRequest;
import com.healthcare.backend.dto.request.ChangePasswordRequest;
import com.healthcare.backend.dto.request.ForgotPasswordRequest;
import com.healthcare.backend.dto.request.RegisterRequest;
import com.healthcare.backend.dto.request.ResetPasswordRequest;
import com.healthcare.backend.dto.response.AuthResponse;
import com.healthcare.backend.dto.response.RegisterResponse;
import com.healthcare.backend.security.UserPrincipal;
import com.healthcare.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @PostMapping("/register/verification-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok("Verify successfully.");
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
        @RequestParam String token,
        @Valid @RequestBody ResetPasswordRequest resetPasswordRequest
    ) {
        authService.executeResetPassword(token, resetPasswordRequest);
        return ResponseEntity.ok("Password reset successfully.");
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @Valid @RequestBody ChangePasswordRequest request
    ) {
        
        authService.changePassword(userPrincipal.email(), request);
        return ResponseEntity.noContent().build();
    }
}
