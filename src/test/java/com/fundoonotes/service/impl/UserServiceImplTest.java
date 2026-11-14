package com.fundoonotes.service.impl;

import com.fundoonotes.dto.request.RegisterRequest;
import com.fundoonotes.dto.response.UserResponse;
import com.fundoonotes.entity.User;
import com.fundoonotes.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : User Management - Service Test
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Tests the business logic of user registration.
 * Checks successful registration, duplicate email handling,
 * email normalization, and password encryption.
 */

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    // Creates a mock repository so the test does not access MySQL.
    @Mock
    private UserRepository userRepository;

    // Creates a mock password encoder for testing password hashing.
    @Mock
    private PasswordEncoder passwordEncoder;

    // Injects the mock dependencies into the service being tested.
    @InjectMocks
    private UserServiceImpl userService;

    // Tests successful registration of a new user.
    @Test
    void registerUserSuccessfully() {

        // Prepare the registration request.
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Hemang");
        request.setLastName("Shah");
        request.setEmail("hemang@example.com");
        request.setPassword("MyPassword123");

        // Simulate that the email does not already exist.
        when(userRepository.existsByEmail("hemang@example.com"))
                .thenReturn(false);

        // Simulate password encryption.
        when(passwordEncoder.encode("MyPassword123"))
                .thenReturn("encoded-password");

        // Simulate saving the user and returning a generated ID.
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            user.setCreatedAt(LocalDateTime.now());
            return user;
        });

        // Call the registration service.
        UserResponse response = userService.registerUser(request);

        // Verify that the response contains the correct user details.
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Hemang", response.getFirstName());
        assertEquals("Shah", response.getLastName());
        assertEquals("hemang@example.com", response.getEmail());

        // Verify that the password was encoded and the user was saved.
        verify(passwordEncoder).encode("MyPassword123");
        verify(userRepository).save(any(User.class));
    }

    // Tests that registration fails when the email already exists.
    @Test
    void registerUserWithDuplicateEmail() {

        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Hemang");
        request.setLastName("Shah");
        request.setEmail("hemang@example.com");
        request.setPassword("MyPassword123");

        // Simulate an existing email address.
        when(userRepository.existsByEmail("hemang@example.com"))
                .thenReturn(true);

        // Verify that the service throws a conflict exception.
        assertThrows(
                ResponseStatusException.class,
                () -> userService.registerUser(request)
        );

        // Verify that no new user is saved.
        verify(userRepository, never()).save(any(User.class));
    }

    // Tests that uppercase email and extra spaces are normalized.
    @Test
    void registerUserNormalizesEmail() {

        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Hemang");
        request.setLastName("Shah");
        request.setEmail("  HEMANG@EXAMPLE.COM  ");
        request.setPassword("MyPassword123");

        // The service should check the normalized email.
        when(userRepository.existsByEmail("hemang@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("MyPassword123"))
                .thenReturn("encoded-password");

        // Simulate saving the normalized user.
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            user.setCreatedAt(LocalDateTime.now());
            return user;
        });

        UserResponse response = userService.registerUser(request);

        // Confirm that the returned email is lowercase and trimmed.
        assertEquals("hemang@example.com", response.getEmail());
    }
}
