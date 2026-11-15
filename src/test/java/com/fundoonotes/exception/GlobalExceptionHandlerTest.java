
package com.fundoonotes.exception;

import com.fundoonotes.dto.request.RegisterRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Global Exception Handling - Test
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Tests the global exception handler independently.
 * Verifies validation errors, ResponseStatusException,
 * invalid JSON, and unexpected server exceptions.
 */

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    // Initializes MockMvc with a sample controller and global handler.
    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // Tests whether invalid registration data returns HTTP 400.
    @Test
    void shouldHandleValidationErrors() throws Exception {

        String invalidRequest = """
                {
                    "firstName": "",
                    "lastName": "Shah",
                    "email": "invalid-email",
                    "password": "123"
                }
                """;

        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Request validation failed"))
                .andExpect(jsonPath("$.path").value("/test/validate"))
                .andExpect(jsonPath("$.validationErrors.firstName")
                        .value("First name is required"))
                .andExpect(jsonPath("$.validationErrors.email")
                        .value("Please provide a valid email address"));
    }

    // Tests whether ResponseStatusException returns its correct HTTP status.
    @Test
    void shouldHandleResponseStatusException() throws Exception {

        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Note not found"))
                .andExpect(jsonPath("$.path").value("/test/not-found"));
    }

    // Tests whether invalid JSON returns HTTP 400.
    @Test
    void shouldHandleInvalidJson() throws Exception {

        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Request body is missing or contains invalid JSON"));
    }

    // Tests whether unexpected exceptions return HTTP 500.
    @Test
    void shouldHandleUnexpectedException() throws Exception {

        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error")
                        .value("Internal Server Error"))
                .andExpect(jsonPath("$.message")
                        .value("An unexpected error occurred"))
                .andExpect(jsonPath("$.path").value("/test/unexpected"));
    }

    // Temporary controller used only to test exception handling.
    @RestController
    static class TestController {

        // Endpoint that triggers a validation exception.
        @PostMapping("/test/validate")
        public String validate(
                @Valid @RequestBody RegisterRequest request) {

            return "Valid request";
        }

        // Endpoint that triggers a 404 ResponseStatusException.
        @GetMapping("/test/not-found")
        public String notFound() {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Note not found"
            );
        }

        // Endpoint that triggers an unexpected server exception.
        @GetMapping("/test/unexpected")
        public String unexpected() {

            throw new IllegalStateException(
                    "Internal test exception"
            );
        }
    }
}
