
package com.fundoonotes.dto.response;

import java.time.Instant;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Global Exception Handling - Error Response DTO
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Defines a common structure for error responses returned
 * by the REST APIs whenever an exception occurs.
 * Contains the timestamp, HTTP status, error type,
 * message, request path, and validation errors.
 */

@Getter
@AllArgsConstructor
public class ErrorResponse {

    // Date and time when the error occurred.
    private Instant timestamp;

    // HTTP status code, such as 400, 404, or 500.
    private int status;

    // HTTP error description.
    private String error;

    // Detailed error message.
    private String message;

    // API endpoint where the error occurred.
    private String path;

    // Field-specific validation errors, if any.
    private Map<String, String> validationErrors;
}
