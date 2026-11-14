package com.fundoonotes.service;

import com.fundoonotes.dto.request.LoginRequest;
import com.fundoonotes.dto.response.LoginResponse;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Authentication - Service Interface
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Defines authentication operations for the application.
 * Provides the contract for user login functionality.
 */

public interface AuthService {

    // Authenticates a user and returns a JWT token.
    LoginResponse login(LoginRequest request);
}