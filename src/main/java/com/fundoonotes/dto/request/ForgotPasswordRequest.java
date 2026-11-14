package com.fundoonotes.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Password Recovery - Forgot Password Request
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Receives the registered email address for password recovery.
 */

@Getter
@Setter
@NoArgsConstructor
public class ForgotPasswordRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email")
    private String email;
}