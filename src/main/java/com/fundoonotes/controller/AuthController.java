
package com.fundoonotes.controller;

import com.fundoonotes.dto.request.LoginRequest;
import com.fundoonotes.dto.response.LoginResponse;
import com.fundoonotes.service.AuthService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Authentication - Controller
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Handles authentication-related HTTP requests.
 * Provides the login endpoint and returns JWT authentication details.
 */

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Authenticates the user and returns a JWT token.
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = authService.login(request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
