package com.healthcare.backend.service;

public interface EmailService {
    void sendVerificationEmail(String to, String verificationLink);

    void sendForgotPasswordEmail(String to, String resetLink);

    void sendEmail(String to, String email, String subject, String path, String message);
}
