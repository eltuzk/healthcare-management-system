package com.healthcare.backend.service;

import com.healthcare.backend.dto.request.AuthRequest;
import com.healthcare.backend.dto.request.ForgotPasswordRequest;
import com.healthcare.backend.dto.request.RegisterRequest;
import com.healthcare.backend.dto.request.ResetPasswordRequest;
import com.healthcare.backend.dto.response.AuthResponse;
import com.healthcare.backend.dto.response.RegisterResponse;

public interface AuthService {
    RegisterResponse register(RegisterRequest accountRequestDTO);

    void verifyEmail(String token);

    AuthResponse login(AuthRequest authRequest);

    void processForgotPassword(ForgotPasswordRequest forgotPasswordRequest);

    void executeResetPassword(String token, ResetPasswordRequest resetPassworsRequest);
}
