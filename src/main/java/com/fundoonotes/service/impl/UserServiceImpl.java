
package com.fundoonotes.service.impl;

import com.fundoonotes.dto.request.RegisterRequest;
import com.fundoonotes.dto.response.UserResponse;
import com.fundoonotes.entity.User;
import com.fundoonotes.event.UserRegisteredEvent;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.service.UserService;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : User Management - Service Implementation
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Implements the business logic for user registration.
 * Checks duplicate emails, encrypts passwords, saves user details,
 * and publishes a registration event for asynchronous processing.
 */

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger logger =
            LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public UserResponse registerUser(RegisterRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        logger.info("Processing new user registration");

        if (userRepository.existsByEmail(email)) {

            logger.warn(
                    "Registration rejected because email is already registered"
            );

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already registered"
            );
        }

        User user = new User();

        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(email);

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        User savedUser = userRepository.save(user);

        logger.info(
                "User registration successful: userId={}",
                savedUser.getId()
        );

        eventPublisher.publishEvent(
                new UserRegisteredEvent(
                        savedUser.getId(),
                        savedUser.getCreatedAt()
                )
        );

        return new UserResponse(
                savedUser.getId(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getEmail(),
                savedUser.getCreatedAt()
        );
    }
}
