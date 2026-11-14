package com.fundoonotes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Password Recovery - Reset Password Request
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Receives the password reset token and the new password.
 * Validates the new password before processing the reset.
 */

@Getter
@Setter
@NoArgsConstructor
public class ResetPasswordRequest {

    @NotBlank(message = "Reset token is required")
    private String token;

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must contain at least 8 characters")
    private String newPassword;
}