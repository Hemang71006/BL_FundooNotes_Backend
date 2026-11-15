
package com.fundoonotes.controller;

import tools.jackson.databind.ObjectMapper;
import com.fundoonotes.dto.request.RegisterRequest;
import com.fundoonotes.dto.response.UserResponse;
import com.fundoonotes.service.UserService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fundoonotes.security.JwtService;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.redis.RedisService;
/**
 * Project Name : Fundoo Notes Backend
 * Module       : User Management - Controller Test
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Tests the user registration REST endpoint.
 * Checks successful registration and invalid registration requests.
 */

@WebMvcTest(UserController.class)
class UserControllerTest {

    // Provides the ability to send HTTP requests to the controller.
    @Autowired
    private MockMvc mockMvc;

    // Converts Java objects into JSON request bodies.
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RedisService redisService;

    // Mocks the service because this test focuses only on the controller.
    @MockitoBean
    private UserService userService;

    // Provides a mock JWT service for the JWT authentication filter.
    @MockitoBean
    private JwtService jwtService;

    // Provides a mock repository for the JWT authentication filter.
    @MockitoBean
    private UserRepository userRepository;

    // Tests whether valid registration returns HTTP 201.
    @Test
    void registerUserSuccessfully() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Hemang");
        request.setLastName("Shah");
        request.setEmail("hemang@example.com");
        request.setPassword("MyPassword123");

        // Prepare the response returned by the mocked service.
        UserResponse response = new UserResponse(
                1L,
                "Hemang",
                "Shah",
                "hemang@example.com",
                LocalDateTime.now()
        );

        when(userService.registerUser(any(RegisterRequest.class)))
                .thenReturn(response);

        // Send a POST request and verify the response.
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("hemang@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    // Tests that an invalid email address is rejected.
    @Test
    void registerUserWithInvalidEmail() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Hemang");
        request.setLastName("Shah");
        request.setEmail("invalid-email");
        request.setPassword("MyPassword123");

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // Tests that a password shorter than eight characters is rejected.
    @Test
    void registerUserWithShortPassword() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Hemang");
        request.setLastName("Shah");
        request.setEmail("hemang@example.com");
        request.setPassword("123");

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // Tests that a blank first name is rejected.
    @Test
    void registerUserWithBlankFirstName() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setFirstName(" ");
        request.setLastName("Shah");
        request.setEmail("hemang@example.com");
        request.setPassword("MyPassword123");

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
