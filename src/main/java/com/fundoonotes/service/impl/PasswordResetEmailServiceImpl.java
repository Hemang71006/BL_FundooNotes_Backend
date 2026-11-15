package com.fundoonotes.service.impl;

import com.fundoonotes.service.PasswordResetEmailService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordResetEmailServiceImpl
        implements PasswordResetEmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.password-reset.expiration-minutes:15}")
    private long expirationMinutes;

    @Override
    public void sendPasswordResetOtp(String email, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("Fundoo Notes - Password Reset OTP");

        message.setText(
                "Hello,\n\n" +
                        "We received a request to reset your Fundoo Notes password.\n\n" +
                        "Your password reset OTP is: " + otp + "\n\n" +
                        "This OTP expires in " + expirationMinutes + " minutes.\n\n" +
                        "Do not share this OTP with anyone.\n\n" +
                        "If you did not request this reset, please ignore this email.\n\n" +
                        "Regards,\nFundoo Notes Team"
        );

        mailSender.send(message);
    }
}