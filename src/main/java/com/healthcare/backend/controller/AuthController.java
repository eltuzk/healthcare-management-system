package com.healthcare.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.healthcare.backend.dto.request.AuthRequest;
import com.healthcare.backend.dto.request.ChangePasswordRequest;
import com.healthcare.backend.dto.request.ForgotPasswordRequest;
import com.healthcare.backend.dto.request.GoogleLoginRequest;
import com.healthcare.backend.dto.request.RegisterRequest;
import com.healthcare.backend.dto.request.ResetPasswordRequest;
import com.healthcare.backend.dto.response.AuthResponse;
import com.healthcare.backend.dto.response.RegisterResponse;
import com.healthcare.backend.security.UserPrincipal;
import com.healthcare.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
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

    @GetMapping("/register/verification-email")
    public ResponseEntity<String> verifyEmailGet(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok("<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "<meta charset='utf-8'>" +
            "<title>Xác thực tài khoản thành công</title>" +
            "<style>" +
            "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; background-color: #f3f4f6; }" +
            ".card { background: white; padding: 40px; border-radius: 16px; box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1); text-align: center; max-width: 450px; border-top: 5px solid #10B981; }" +
            "h2 { color: #10B981; margin-top: 0; margin-bottom: 12px; font-size: 24px; }" +
            "p { color: #4B5563; margin-bottom: 24px; font-size: 16px; line-height: 1.6; }" +
            ".btn { display: inline-block; padding: 12px 28px; background: linear-gradient(135deg, #0D6EFD 0%, #0043A8 100%); color: white !important; text-decoration: none; border-radius: 8px; font-weight: 600; box-shadow: 0 4px 15px rgba(13, 110, 253, 0.2); transition: all 0.3s ease; }" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<div class='card'>" +
            "<h2>Xác thực thành công!</h2>" +
            "<p>Tài khoản của bạn đã được kích hoạt thành công. Giờ đây bạn đã có thể đăng nhập vào hệ thống The Clinical Curator.</p>" +
            "<a href='https://healthcare.io.vn/login' class='btn'>Đến trang Đăng nhập</a>" +
            "</div>" +
            "</body>" +
            "</html>");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        return ResponseEntity.ok(authService.login(authRequest));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest googleLoginRequest) {
        return ResponseEntity.ok(authService.loginWithGoogle(googleLoginRequest));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        authService.processForgotPassword(forgotPasswordRequest);
        return ResponseEntity.ok("Mail sent successfully. Please check your email.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestParam String token,
            @Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        authService.executeResetPassword(token, resetPasswordRequest);
        return ResponseEntity.ok("Password reset successfully.");
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ChangePasswordRequest request) {

        authService.changePassword(userPrincipal.email(), request);
        return ResponseEntity.noContent().build();
    }
}
