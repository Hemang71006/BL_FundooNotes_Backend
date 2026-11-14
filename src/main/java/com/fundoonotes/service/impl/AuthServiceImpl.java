package com.fundoonotes.service.impl;

import com.fundoonotes.dto.request.LoginRequest;
import com.fundoonotes.dto.response.LoginResponse;
import com.fundoonotes.entity.User;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.security.JwtService;
import com.fundoonotes.service.AuthService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Locale;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Authentication - Service Implementation
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Implements user authentication functionality.
 * Verifies login credentials using BCrypt and generates JWT tokens.
 */

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // Authenticates the user and generates a JWT token.
    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        // Normalize the email before searching the database.
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        // Find the user using the email address.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"
                ));

        // Verify the entered password against the stored BCrypt hash.
        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        // Reject login if the password does not match.
        if (!passwordMatches) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        // Generate a JWT token after successful authentication.
        String token = jwtService.generateToken(user.getEmail());

        // Return the token and authenticated user's email.
        return new LoginResponse(
                token,
                "Bearer",
                user.getEmail(),
                "Login successful"
        );
    }
}