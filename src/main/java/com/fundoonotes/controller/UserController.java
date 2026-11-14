
package com.fundoonotes.controller;

import com.fundoonotes.dto.request.RegisterRequest;
import com.fundoonotes.dto.response.UserResponse;
import com.fundoonotes.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : User Management - Controller
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Exposes REST endpoints for user management.
 * Handles incoming user registration requests and returns
 * the registration result to the client.
 */

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    // Injects the service responsible for user registration.
    private final UserService userService;

    // Handles the POST request for registering a new user.
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(
            @Valid @RequestBody RegisterRequest request) {

        // Calls the service to register the user.
        UserResponse response = userService.registerUser(request);

        // Returns HTTP 201 Created with the registered user details.
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
