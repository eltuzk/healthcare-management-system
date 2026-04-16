package com.healthcare.backend.service;

import com.healthcare.backend.dto.request.AuthRequestDTO;
import com.healthcare.backend.dto.request.ForgotPassword_EmailRequestDTO;
import com.healthcare.backend.dto.request.RegisterRequestDTO;
import com.healthcare.backend.dto.request.ResetPasswordRequestDTO;
import com.healthcare.backend.dto.response.AuthResponseDTO;
import com.healthcare.backend.dto.response.RegisterResponseDTO;

public interface AuthServiceInterface {
    RegisterResponseDTO register(RegisterRequestDTO accountRequestDTO);

    void verifyEmail(String token);

    AuthResponseDTO login(AuthRequestDTO authRequestDTO);

    void processForgotPassword(ForgotPassword_EmailRequestDTO forgotPasswordRequest);

    void executeResetPassword(String token, ResetPasswordRequestDTO resetPassworsRequest);
}
