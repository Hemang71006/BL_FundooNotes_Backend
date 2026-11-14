
package com.fundoonotes.controller;

import com.fundoonotes.dto.request.LoginRequest;
import com.fundoonotes.dto.response.LoginResponse;
import com.fundoonotes.service.AuthService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fundoonotes.security.JwtService;
import com.fundoonotes.repository.UserRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Authentication - Controller Tests
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Tests the authentication REST controller.
 * Verifies login responses and request validation.
 */

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    // Provides a mock JWT service for the JWT authentication filter.
    @MockitoBean
    private JwtService jwtService;

    // Provides a mock repository for the JWT authentication filter.
    @MockitoBean
    private UserRepository userRepository;

    @Test
    void loginShouldReturnTokenWhenCredentialsAreValid() throws Exception {

        LoginResponse response = new LoginResponse(
                "test-jwt-token",
                "Bearer",
                "hemang@example.com",
                "Login successful"
        );

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(response);

        String requestBody = """
                {
                    "email": "hemang@example.com",
                    "password": "Password@123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value("hemang@example.com"))
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    void loginShouldReturnBadRequestWhenEmailIsInvalid() throws Exception {

        String requestBody = """
                {
                    "email": "invalid-email",
                    "password": "Password@123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginShouldReturnBadRequestWhenPasswordIsMissing() throws Exception {

        String requestBody = """
                {
                    "email": "hemang@example.com",
                    "password": ""
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}
