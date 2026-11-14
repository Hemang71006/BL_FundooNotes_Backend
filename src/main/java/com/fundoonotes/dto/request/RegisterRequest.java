
package com.fundoonotes.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : User Management - Request DTO
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Receives user registration details from the client.
 * Validates the input before passing it to the service layer.
 */

@Getter
@Setter
@NoArgsConstructor
public class RegisterRequest {

    // First name is required and cannot exceed 50 characters.
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    // Last name is required and cannot exceed 50 characters.
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    // Email must be present and follow a valid email format.
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    // Password must contain between 8 and 100 characters.
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;
}
