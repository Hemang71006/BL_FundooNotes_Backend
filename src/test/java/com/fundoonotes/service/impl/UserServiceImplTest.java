
package com.fundoonotes.service.impl;

import com.fundoonotes.dto.request.RegisterRequest;
import com.fundoonotes.dto.response.UserResponse;
import com.fundoonotes.entity.User;
import com.fundoonotes.event.UserRegisteredEvent;
import com.fundoonotes.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.context.ApplicationEventPublisher;
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
 * email normalization, password encryption, and event publishing.
 */

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void registerUserSuccessfully() {

        RegisterRequest request = new RegisterRequest();

        request.setFirstName("Hemang");
        request.setLastName("Shah");
        request.setEmail("hemang@example.com");
        request.setPassword("MyPassword123");

        when(userRepository.existsByEmail("hemang@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("MyPassword123"))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {

            User user = invocation.getArgument(0);

            user.setId(1L);
            user.setCreatedAt(LocalDateTime.now());

            return user;
        });

        UserResponse response = userService.registerUser(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Hemang", response.getFirstName());
        assertEquals("Shah", response.getLastName());
        assertEquals("hemang@example.com", response.getEmail());

        verify(passwordEncoder).encode("MyPassword123");

        verify(userRepository).save(any(User.class));

        verify(eventPublisher).publishEvent(
                any(UserRegisteredEvent.class)
        );
    }

    @Test
    void registerUserWithDuplicateEmail() {

        RegisterRequest request = new RegisterRequest();

        request.setFirstName("Hemang");
        request.setLastName("Shah");
        request.setEmail("hemang@example.com");
        request.setPassword("MyPassword123");

        when(userRepository.existsByEmail("hemang@example.com"))
                .thenReturn(true);

        assertThrows(
                ResponseStatusException.class,
                () -> userService.registerUser(request)
        );

        verify(userRepository, never()).save(any(User.class));

        verify(eventPublisher, never()).publishEvent(
                any(UserRegisteredEvent.class)
        );
    }

    @Test
    void registerUserNormalizesEmail() {

        RegisterRequest request = new RegisterRequest();

        request.setFirstName("Hemang");
        request.setLastName("Shah");
        request.setEmail("  HEMANG@EXAMPLE.COM  ");
        request.setPassword("MyPassword123");

        when(userRepository.existsByEmail("hemang@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("MyPassword123"))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {

            User user = invocation.getArgument(0);

            user.setId(2L);
            user.setCreatedAt(LocalDateTime.now());

            return user;
        });

        UserResponse response = userService.registerUser(request);

        assertEquals("hemang@example.com", response.getEmail());

        verify(eventPublisher).publishEvent(
                any(UserRegisteredEvent.class)
        );
    }
}
