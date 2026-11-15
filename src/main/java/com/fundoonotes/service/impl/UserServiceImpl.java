
package com.fundoonotes.service.impl;

import com.fundoonotes.dto.request.RegisterRequest;
import com.fundoonotes.dto.response.UserResponse;
import com.fundoonotes.entity.User;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : User Management - Service Implementation
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Implements the business logic for user registration.
 * Checks duplicate emails, encrypts passwords, saves user details,
 * and returns a safe response to the controller.
 */

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger logger =
            LoggerFactory.getLogger(UserServiceImpl.class);
    // Lombok generates a constructor to inject these dependencies.
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Executes the complete registration operation in a transaction.
    @Override
    @Transactional
    public UserResponse registerUser(RegisterRequest request) {

        // Remove extra spaces and convert email to lowercase.
        String email = request.getEmail().trim().toLowerCase();

        logger.info("Processing new user registration");
        // Stop registration if the email is already present in the database.
        if (userRepository.existsByEmail(email)) {
            logger.warn("Registration rejected because email is already registered");
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already registered"
            );
        }

        // Create a new User entity to store registration information.
        User user = new User();

        // Store the user's name after removing unnecessary spaces.
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());

        // Store the normalized email address.
        user.setEmail(email);

        // Convert the plain-text password into a BCrypt hash.
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Save the user in the database.
        User savedUser = userRepository.save(user);

        logger.info(
                "User registration successful: userId={}",
                savedUser.getId()
        );

        // Return user details without exposing the password.
        return new UserResponse(
                savedUser.getId(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getEmail(),
                savedUser.getCreatedAt()
        );
    }
}
