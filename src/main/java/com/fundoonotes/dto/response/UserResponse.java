
package com.fundoonotes.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : User Management - Response DTO
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Defines the response returned after successful user registration.
 * Excludes the password to prevent sensitive information from
 * being exposed to the client.
 */

@Getter
@AllArgsConstructor
public class UserResponse {

    // Database-generated user ID.
    private Long id;

    // Registered user's first name.
    private String firstName;

    // Registered user's last name.
    private String lastName;

    // Registered user's email address.
    private String email;

    // Date and time when the account was created.
    private LocalDateTime createdAt;
}
