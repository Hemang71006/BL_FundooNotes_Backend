package com.fundoonotes.service.impl;

import com.fundoonotes.service.PasswordResetEmailService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Password Recovery - Email Service Implementation
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Sends password reset links to registered users through email.
 */

@Service
@RequiredArgsConstructor
public class PasswordResetEmailServiceImpl
        implements PasswordResetEmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Override
    public void sendPasswordResetEmail(
            String email,
            String resetLink) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("Fundoo Notes - Password Reset");

        message.setText(
                "Hello,\n\n" +
                        "We received a request to reset your Fundoo Notes password.\n\n" +
                        "Use the link below to reset your password:\n" +
                        resetLink + "\n\n" +
                        "This link expires in 15 minutes.\n\n" +
                        "If you did not request this reset, you can ignore this email.\n\n" +
                        "Regards,\nFundoo Notes Team"
        );

        mailSender.send(message);
    }
}