package com.fundoonotes.service.impl;

import com.fundoonotes.entity.PasswordResetToken;
import com.fundoonotes.entity.User;
import com.fundoonotes.repository.PasswordResetTokenRepository;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.service.PasswordResetEmailService;
import com.fundoonotes.service.PasswordResetService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Password Recovery - Service Implementation
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Generates secure password reset tokens, sends reset links,
 * validates token expiry, and updates encrypted user passwords.
 */

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetEmailService emailService;

    @Value("${app.password-reset.url}")
    private String resetUrl;

    @Value("${app.password-reset.expiration-minutes:15}")
    private long expirationMinutes;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public void requestPasswordReset(String email) {

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElse(null);

        // Return normally even when the email does not exist.
        // This prevents exposing which email addresses are registered.
        if (user == null) {
            return;
        }

        // Remove previous reset tokens for this user.
        tokenRepository.deleteAllByUser_Id(user.getId());

        // Generate a cryptographically secure random token.
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        // Store only the SHA-256 hash in the database.
        PasswordResetToken resetToken = new PasswordResetToken();

        resetToken.setTokenHash(hashToken(rawToken));
        resetToken.setUser(user);
        resetToken.setExpiresAt(
                LocalDateTime.now().plusMinutes(expirationMinutes)
        );
        resetToken.setUsed(false);

        tokenRepository.save(resetToken);

        // Send the original token to the user's email.
        String resetLink = resetUrl + "?token=" + rawToken;

        emailService.sendPasswordResetEmail(
                normalizedEmail,
                resetLink
        );
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {

        String tokenHash = hashToken(token);

        PasswordResetToken resetToken =
                tokenRepository.findByTokenHash(tokenHash)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Invalid or expired password reset token"
                        ));

        // Reject tokens that have already been used.
        if (resetToken.isUsed()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password reset token has already been used"
            );
        }

        // Reject expired tokens.
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password reset token has expired"
            );
        }

        User user = resetToken.getUser();

        // Encrypt the new password before saving it.
        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);

        // Mark the token as used so it cannot be reused.
        resetToken.setUsed(true);

        tokenRepository.save(resetToken);
    }

    // Converts the raw reset token into a SHA-256 hash.
    private String hashToken(String token) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return Base64.getEncoder().encodeToString(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "Unable to process password reset token",
                    exception
            );
        }
    }
}