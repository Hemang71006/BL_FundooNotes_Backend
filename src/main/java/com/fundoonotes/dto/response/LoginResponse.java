package com.fundoonotes.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Authentication - Login Response DTO
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Returns the authentication result to the client.
 * Contains the generated JWT token and user information.
 */

@Getter
@AllArgsConstructor
public class LoginResponse {

    // Contains the generated JWT token.
    private String token;

    // Specifies the token authentication scheme.
    private String tokenType;

    // Contains the authenticated user's email.
    private String email;

    // Contains the login success message.
    private String message;
}