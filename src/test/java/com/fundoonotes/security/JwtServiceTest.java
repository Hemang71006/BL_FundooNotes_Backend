
package com.fundoonotes.security;

import io.jsonwebtoken.JwtException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Authentication - JWT Service Tests
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Tests JWT token generation and validation.
 * Verifies email extraction and rejection of invalid tokens.
 */

class JwtServiceTest {

    private JwtService jwtService;

    // A test-only Base64 secret containing 32 bytes.
    private static final String TEST_SECRET =
            "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        // Set the properties normally injected from application.properties.
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 60000L);
    }

    @Test
    void generateTokenShouldReturnValidToken() {

        String token = jwtService.generateToken("hemang@example.com");

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void extractEmailShouldReturnCorrectEmail() {

        String token = jwtService.generateToken("hemang@example.com");

        String email = jwtService.extractEmail(token);

        assertEquals("hemang@example.com", email);
    }

    @Test
    void isTokenValidShouldReturnTrueForCorrectEmail() {

        String token = jwtService.generateToken("hemang@example.com");

        boolean result = jwtService.isTokenValid(
                token,
                "hemang@example.com"
        );

        assertTrue(result);
    }

    @Test
    void isTokenValidShouldReturnFalseForDifferentEmail() {

        String token = jwtService.generateToken("hemang@example.com");

        boolean result = jwtService.isTokenValid(
                token,
                "another@example.com"
        );

        assertFalse(result);
    }

    @Test
    void extractEmailShouldRejectInvalidToken() {

        String invalidToken = "this-is-not-a-valid-jwt";

        assertThrows(
                JwtException.class,
                () -> jwtService.extractEmail(invalidToken)
        );
    }
}
