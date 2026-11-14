package com.fundoonotes.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Label Management - Response DTO
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Returns label information to the client.
 * Does not expose internal user or database relationship details.
 */

@Getter
@AllArgsConstructor
public class LabelResponse {

    private Long id;
    private String name;
    private String color;
}