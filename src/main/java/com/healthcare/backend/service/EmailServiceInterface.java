package com.healthcare.backend.service;

public interface EmailServiceInterface {
    void sendVerificationEmail(String to, String verificationLink);

    void sendForgotPasswordEmail(String to, String resetLink);

    void sendEmail(String to, String email, String subject, String path, String message);
}
