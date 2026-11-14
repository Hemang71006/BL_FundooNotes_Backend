package com.fundoonotes.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Authentication - Login Request DTO
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Receives login credentials from the client.
 * Validates the email and password before authentication.
 */

@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {

    // Stores the email entered by the user.
    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email")
    private String email;

    // Stores the password entered by the user.
    @NotBlank(message = "Password is required")
    private String password;
}