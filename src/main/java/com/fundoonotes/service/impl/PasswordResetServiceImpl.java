package com.fundoonotes.service.impl;

import com.fundoonotes.entity.User;
import com.fundoonotes.redis.RedisService;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.service.PasswordResetEmailService;
import com.fundoonotes.service.PasswordResetService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetEmailService emailService;
    private final RedisService redisService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.password-reset.expiration-minutes:15}")
    private long expirationMinutes;

    @Override
    @Transactional
    public void requestPasswordReset(String email) {

        String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElse(null);

        // Do not reveal whether the email is registered.
        if (user == null) {
            return;
        }

        // Generate a secure six-digit OTP.
        String otp = String.format(
                Locale.ROOT,
                "%06d",
                secureRandom.nextInt(1_000_000)
        );

        // Store the OTP in Redis with an expiry time.
        redisService.storeOtp(
                normalizedEmail,
                otp,
                Duration.ofMinutes(expirationMinutes)
        );

        // Send the OTP to the registered email.
        emailService.sendPasswordResetOtp(
                normalizedEmail,
                otp
        );
    }

    @Override
    @Transactional
    public void resetPassword(
            String email,
            String otp,
            String newPassword) {

        String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid or expired OTP"
                ));

        String storedOtp = redisService.getOtp(normalizedEmail);

        if (storedOtp == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "OTP has expired or does not exist"
            );
        }

        // Compare OTP values without ordinary string comparison.
        boolean validOtp = MessageDigest.isEqual(
                storedOtp.getBytes(StandardCharsets.UTF_8),
                otp.getBytes(StandardCharsets.UTF_8)
        );

        if (!validOtp) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid OTP"
            );
        }

        // Encrypt and save the new password.
        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);

        // Delete the OTP only after the database transaction commits.
        if (TransactionSynchronizationManager.isSynchronizationActive()) {

            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {

                        @Override
                        public void afterCommit() {
                            redisService.deleteOtp(normalizedEmail);
                        }
                    }
            );

        } else {
            redisService.deleteOtp(normalizedEmail);
        }
    }
}