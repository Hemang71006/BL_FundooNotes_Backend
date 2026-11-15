package com.fundoonotes.redis;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

// Provides reusable operations for storing temporary data in Redis.
@Service
public class RedisService {

    private static final String OTP_PREFIX = "fundoo:otp:";
    private static final String BLACKLIST_PREFIX = "fundoo:blacklist:";

    private final RedisTemplate<String, String> redisTemplate;

    // Injects the configured Redis template.
    public RedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Stores an OTP with an automatic expiration time.
    public void storeOtp(String email, String otp, Duration expiry) {
        redisTemplate.opsForValue().set(
                OTP_PREFIX + email.toLowerCase(),
                otp,
                expiry
        );
    }

    // Retrieves the OTP associated with an email address.
    public String getOtp(String email) {
        return redisTemplate.opsForValue().get(
                OTP_PREFIX + email.toLowerCase()
        );
    }

    // Deletes an OTP after successful verification.
    public void deleteOtp(String email) {
        redisTemplate.delete(OTP_PREFIX + email.toLowerCase());
    }

    // Stores a hashed JWT identifier in the blacklist until its expiry.
    public void blacklistToken(String token, Duration remainingValidity) {
        if (remainingValidity == null || remainingValidity.isZero()
                || remainingValidity.isNegative()) {
            return;
        }

        String tokenHash = hashToken(token);

        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + tokenHash,
                "REVOKED",
                remainingValidity
        );
    }

    // Checks whether a JWT token has been blacklisted.
    public boolean isTokenBlacklisted(String token) {
        String tokenHash = hashToken(token);

        return Boolean.TRUE.equals(
                redisTemplate.hasKey(BLACKLIST_PREFIX + tokenHash)
        );
    }

    // Removes a token from the blacklist.
    public void removeBlacklistedToken(String token) {
        String tokenHash = hashToken(token);

        redisTemplate.delete(BLACKLIST_PREFIX + tokenHash);
    }

    // Hashes tokens so the original JWT is not stored in Redis.
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", exception);
        }
    }
}