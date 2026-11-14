
package com.fundoonotes.service.impl;

import com.fundoonotes.dto.request.LoginRequest;
import com.fundoonotes.dto.response.LoginResponse;
import com.fundoonotes.entity.User;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.security.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Authentication - Service Tests
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Tests user authentication service functionality.
 * Verifies successful login and invalid credential handling.
 */

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("hemang@example.com");
        user.setPassword("$2a$10$hashedPassword");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("hemang@example.com");
        loginRequest.setPassword("Password@123");
    }

    @Test
    void loginShouldReturnTokenWhenCredentialsAreCorrect() {

        when(userRepository.findByEmail("hemang@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "Password@123",
                "$2a$10$hashedPassword"
        )).thenReturn(true);

        when(jwtService.generateToken("hemang@example.com"))
                .thenReturn("test-jwt-token");

        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("test-jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("hemang@example.com", response.getEmail());
        assertEquals("Login successful", response.getMessage());

        verify(userRepository).findByEmail("hemang@example.com");
        verify(passwordEncoder).matches(
                "Password@123",
                "$2a$10$hashedPassword"
        );
        verify(jwtService).generateToken("hemang@example.com");
    }

    @Test
    void loginShouldThrowExceptionWhenPasswordIsIncorrect() {

        when(userRepository.findByEmail("hemang@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "WrongPassword",
                "$2a$10$hashedPassword"
        )).thenReturn(false);

        loginRequest.setPassword("WrongPassword");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());

        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    void loginShouldThrowExceptionWhenEmailDoesNotExist() {

        when(userRepository.findByEmail("hemang@example.com"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(anyString());
    }
}
