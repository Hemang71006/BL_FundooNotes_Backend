package com.fundoonotes.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

// Tests Redis service methods using mocked Redis operations.
@ExtendWith(MockitoExtension.class)
class RedisServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisService redisService;

    // Creates the service before each test without unnecessary mock stubbing.
    @BeforeEach
    void setUp() {
        redisService = new RedisService(redisTemplate);
    }

    // Tests storing an OTP with an expiry.
    @Test
    void storeOtpShouldSaveOtpWithExpiry() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        redisService.storeOtp(
                "test@example.com",
                "123456",
                Duration.ofMinutes(5)
        );

        verify(valueOperations).set(
                "fundoo:otp:test@example.com",
                "123456",
                Duration.ofMinutes(5)
        );
    }

    // Tests retrieving an OTP using its email address.
    @Test
    void getOtpShouldReturnStoredOtp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("fundoo:otp:test@example.com"))
                .thenReturn("123456");

        String result = redisService.getOtp("test@example.com");

        assertEquals("123456", result);
    }

    // Tests deleting an OTP from Redis.
    @Test
    void deleteOtpShouldRemoveOtp() {
        redisService.deleteOtp("test@example.com");

        verify(redisTemplate).delete("fundoo:otp:test@example.com");
    }

    // Tests storing a hashed JWT with its remaining validity.
    @Test
    void blacklistTokenShouldStoreHashedTokenWithExpiry() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        redisService.blacklistToken(
                "sample.jwt.token",
                Duration.ofHours(1)
        );

        verify(valueOperations).set(
                startsWith("fundoo:blacklist:"),
                eq("REVOKED"),
                eq(Duration.ofHours(1))
        );
    }

    // Tests that a token with invalid expiry is not stored.
    @Test
    void blacklistTokenShouldIgnoreInvalidExpiry() {
        redisService.blacklistToken(
                "sample.jwt.token",
                Duration.ZERO
        );

        org.mockito.Mockito.verifyNoInteractions(redisTemplate, valueOperations);
    }

    // Tests detecting a blacklisted JWT.
    @Test
    void isTokenBlacklistedShouldReturnTrueWhenTokenExists() {
        when(redisTemplate.hasKey(anyString())).thenReturn(true);

        boolean result = redisService.isTokenBlacklisted("sample.jwt.token");

        assertTrue(result);
    }

    // Tests detecting a JWT that is not blacklisted.
    @Test
    void isTokenBlacklistedShouldReturnFalseWhenTokenDoesNotExist() {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);

        boolean result = redisService.isTokenBlacklisted("sample.jwt.token");

        assertFalse(result);
    }

    // Tests removing a JWT from the blacklist.
    @Test
    void removeBlacklistedTokenShouldDeleteTokenKey() {
        redisService.removeBlacklistedToken("sample.jwt.token");

        verify(redisTemplate).delete(
                startsWith("fundoo:blacklist:")
        );
    }
}