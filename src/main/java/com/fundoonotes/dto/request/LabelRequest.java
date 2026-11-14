package com.fundoonotes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Label Management - Request DTO
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Carries label information submitted by the client.
 * Validates the label name and optional color.
 */

@Getter
@Setter
public class LabelRequest {

    @NotBlank(message = "Label name is required")
    @Size(max = 50, message = "Label name cannot exceed 50 characters")
    private String name;

    @Size(max = 7, message = "Color cannot exceed 7 characters")
    private String color;
}