
package com.fundoonotes.service;

import com.fundoonotes.dto.request.RegisterRequest;
import com.fundoonotes.dto.response.UserResponse;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : User Management - Service Interface
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Declares the business operations supported by user management.
 * Separates the controller from the service implementation.
 */

public interface UserService {

    // Registers a new user and returns the registered user's details.
    UserResponse registerUser(RegisterRequest request);
}
