package com.fundoonotes.controller;

import com.fundoonotes.dto.request.ForgotPasswordRequest;
import com.fundoonotes.dto.request.ResetPasswordRequest;
import com.fundoonotes.service.PasswordResetService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Password Recovery - Controller
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Exposes REST endpoints for requesting password recovery
 * and resetting a user's password.
 */

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    // Sends a password reset link to the registered email.
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        passwordResetService.requestPasswordReset(request.getEmail());

        return ResponseEntity.ok(
                "If the email is registered, a password reset link has been sent."
        );
    }

    // Validates the reset token and updates the user's password.
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        passwordResetService.resetPassword(
                request.getToken(),
                request.getNewPassword()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body("Password has been reset successfully");
    }
}