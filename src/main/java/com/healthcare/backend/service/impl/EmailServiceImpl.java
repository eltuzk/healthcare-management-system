package com.healthcare.backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.healthcare.backend.service.EmailServiceInterface;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailServiceInterface {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendVerificationEmail(String email, String verificationToken) {
        String subject = "Email verification";
        String path = "/auth/register/verification-email";
        String message = "Please click the button below to verify your email: ";

        sendEmail(email, verificationToken, subject, path, message);
    }

    @Override
    public void sendForgotPasswordEmail(String email, String forgotPasswordToken) {
        String subject = "Forgot your password";
        String path = "/auth/forgot-password";
        String message = "Please click the button below to reset your password: ";

        sendEmail(email, forgotPasswordToken, subject, path, message);
    }

    @Override
    public void sendEmail(String email, String token, String subject, String path, String message) {
        try{
            String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path(path)
                    .queryParam("token", token)
                    .toUriString();

            String content = """
                    <div style="font-family: Time New Roman, san-serif; max-width: 600px; margin: auto; padding: 20px; border-radius: 10px; background-color: #f9f9f9;">
                        <h2 style="color: #333;">%s</h2>
                        <p style="font-size: 16px; color: #555;">%s</p>
                        <a href="%s" style="display: inline-block; padding: 10px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 5px;">Click here</a>
                        <p style="font-size: 14px; color: #999;">This link will expire in 24 hours.</p>
                        <p style="font-size: 14px; color: #999;">If you did not request this email, please ignore it.</p>
                        <p style="font-size: 14px; color: #999;">This is an automated email, please do not reply.</p>
                    </div>
                    """.formatted(subject, message, url);
            
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setFrom(fromEmail);
            helper.setText(content, true);
            
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
}
