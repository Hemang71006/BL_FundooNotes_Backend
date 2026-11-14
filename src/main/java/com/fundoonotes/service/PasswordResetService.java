package com.fundoonotes.service;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Password Recovery - Service Interface
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Defines password recovery operations.
 * Supports requesting a reset link and setting a new password.
 */

public interface PasswordResetService {

    void requestPasswordReset(String email);

    void resetPassword(String token, String newPassword);
}