
package com.fundoonotes.config;

import com.fundoonotes.controller.AuthController;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.security.JwtAuthenticationFilter;
import com.fundoonotes.security.JwtService;
import com.fundoonotes.service.AuthService;

import com.fundoonotes.dto.request.LoginRequest;
import com.fundoonotes.dto.response.LoginResponse;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fundoonotes.redis.RedisService;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Authentication - Security Configuration Tests
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Tests the application's Spring Security configuration.
 * Verifies public login access and protection of secured endpoints.
 */

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private RedisService redisService;

    // Mock the JWT service used internally by the real JWT filter.
    @MockitoBean
    private JwtService jwtService;

    // Mock the repository used internally by the real JWT filter.
    @MockitoBean
    private UserRepository userRepository;

    @Test
    void loginEndpointShouldBePublic() throws Exception {

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
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpointShouldRejectRequestWithoutToken() throws Exception {

        mockMvc.perform(get("/api/notes"))
                .andExpect(status().isUnauthorized());
    }
}
