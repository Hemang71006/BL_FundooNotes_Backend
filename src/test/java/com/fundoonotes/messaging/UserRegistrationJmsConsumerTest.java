package com.fundoonotes.messaging;

import com.fundoonotes.entity.User;
import com.fundoonotes.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserRegistrationJmsConsumerTest {

    private UserRepository userRepository;
    private JavaMailSender mailSender;

    private UserRegistrationJmsConsumer consumer;

    @BeforeEach
    void setUp() {

        userRepository = mock(UserRepository.class);
        mailSender = mock(JavaMailSender.class);

        consumer = new UserRegistrationJmsConsumer(
                userRepository,
                mailSender,
                "noreply@example.com"
        );
    }

    @Test
    void shouldSendWelcomeEmailForRegistrationMessage() {

        User user = new User();
        user.setId(10L);
        user.setFirstName("Test");
        user.setEmail("test@example.com");

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(user));

        Map<String, Object> message = Map.of(
                "userId", 10L,
                "eventType", "USER_REGISTERED",
                "registeredAt", "2026-11-15T10:30:00"
        );

        assertDoesNotThrow(
                () -> consumer.receiveRegistrationMessage(message)
        );

        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}