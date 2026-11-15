package com.fundoonotes.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

// Verifies Redis operations against the actual Redis server.
@SpringBootTest
class RedisIntegrationTest {

    @Autowired
    private RedisService redisService;

    // Verifies OTP storage, retrieval, and deletion in Redis.
    @Test
    void shouldStoreRetrieveAndDeleteOtp() {
        String email = "redis-" + UUID.randomUUID() + "@example.com";

        redisService.storeOtp(email, "456789", Duration.ofMinutes(5));

        assertEquals("456789", redisService.getOtp(email));

        redisService.deleteOtp(email);

        assertEquals(null, redisService.getOtp(email));
    }

    // Verifies JWT blacklist storage and removal in Redis.
    @Test
    void shouldBlacklistAndRemoveToken() {
        String token = "test-token-" + UUID.randomUUID();

        redisService.blacklistToken(token, Duration.ofMinutes(10));

        assertTrue(redisService.isTokenBlacklisted(token));

        redisService.removeBlacklistedToken(token);

        assertFalse(redisService.isTokenBlacklisted(token));
    }
}