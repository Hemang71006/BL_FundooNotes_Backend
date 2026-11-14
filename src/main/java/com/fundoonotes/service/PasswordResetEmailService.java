package com.fundoonotes.service;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Password Recovery - Email Service
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Defines the operation used to send password recovery emails.
 */

public interface PasswordResetEmailService {

    void sendPasswordResetEmail(String email, String resetLink);
}