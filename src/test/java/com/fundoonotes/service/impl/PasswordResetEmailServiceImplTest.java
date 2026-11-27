package com.fundoonotes.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

/*
 * Project Name : Fundoo Notes Backend
 * Description  : Verifies reset emails use the configured frontend URL.
 * Author       : Hemang
 * Date         : 27-11-2026
 */
@ExtendWith(MockitoExtension.class)
class PasswordResetEmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Test
    void shouldIncludeConfiguredFrontendUrlWithoutAssumingRoute() {

        PasswordResetEmailServiceImpl service =
                new PasswordResetEmailServiceImpl(mailSender);
        ReflectionTestUtils.setField(
                service, "fromEmail", "noreply@example.com");
        ReflectionTestUtils.setField(
                service, "frontendUrl", "http://localhost:5173");
        ReflectionTestUtils.setField(
                service, "expirationMinutes", 15L);

        service.sendPasswordResetOtp("user@example.com", "123456");

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        String emailText = captor.getValue().getText();
        assertTrue(emailText.contains("123456"));
        assertTrue(emailText.contains("http://localhost:5173"));
        assertFalse(emailText.contains("localhost:3000"));
        assertFalse(emailText.contains("/reset-password"));
    }
}
