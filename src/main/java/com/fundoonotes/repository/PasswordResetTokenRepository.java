package com.fundoonotes.repository;

import com.fundoonotes.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Password Recovery - Token Repository
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Provides database operations for password reset tokens.
 * Supports finding tokens and removing previous reset requests.
 */

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    void deleteAllByUser_Id(Long userId);
}