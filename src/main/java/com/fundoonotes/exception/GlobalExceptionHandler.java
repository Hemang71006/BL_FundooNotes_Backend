
package com.fundoonotes.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fundoonotes.dto.response.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Global Exception Handling
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Handles application-wide exceptions centrally.
 * Converts exceptions into consistent REST API error responses.
 * Handles validation errors, resource errors, invalid requests,
 * database conflicts, and unexpected server exceptions.
 */

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handles validation errors from @Valid request DTOs.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        Map<String, String> validationErrors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        validationErrors.putIfAbsent(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Request validation failed",
                request.getRequestURI(),
                validationErrors
        );
    }

    // Handles invalid or missing JSON request bodies.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequestBody(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {

        return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Request body is missing or contains invalid JSON",
                request.getRequestURI(),
                new LinkedHashMap<>()
        );
    }

    // Handles constraint violations from Jakarta Validation.
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request) {

        Map<String, String> validationErrors = new LinkedHashMap<>();

        exception.getConstraintViolations().forEach(violation ->
                validationErrors.put(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                )
        );

        return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Request validation failed",
                request.getRequestURI(),
                validationErrors
        );
    }

    // Handles ResponseStatusException thrown by existing services.
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(
            ResponseStatusException exception,
            HttpServletRequest request) {

        HttpStatusCode statusCode = exception.getStatusCode();

        String message = exception.getReason() != null
                ? exception.getReason()
                : "Request could not be processed";

        return createErrorResponse(
                statusCode,
                message,
                request.getRequestURI(),
                new LinkedHashMap<>()
        );
    }

    // Handles requests for endpoints that do not exist.
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            NoResourceFoundException exception,
            HttpServletRequest request) {

        return createErrorResponse(
                HttpStatus.NOT_FOUND,
                "Requested resource was not found",
                request.getRequestURI(),
                new LinkedHashMap<>()
        );
    }

    // Handles unsupported HTTP methods.
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request) {

        return createErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                "HTTP method is not supported for this endpoint",
                request.getRequestURI(),
                new LinkedHashMap<>()
        );
    }

    // Handles database constraint and duplicate-data conflicts.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseConflict(
            DataIntegrityViolationException exception,
            HttpServletRequest request) {

        return createErrorResponse(
                HttpStatus.CONFLICT,
                "The request conflicts with existing data",
                request.getRequestURI(),
                new LinkedHashMap<>()
        );
    }

    // Handles all other unexpected exceptions.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception exception,
            HttpServletRequest request) {

        return createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                request.getRequestURI(),
                new LinkedHashMap<>()
        );
    }

    // Creates the common error response for all exception handlers.
    private ResponseEntity<ErrorResponse> createErrorResponse(
            HttpStatusCode statusCode,
            String message,
            String path,
            Map<String, String> validationErrors) {

        HttpStatus resolvedStatus = HttpStatus.resolve(statusCode.value());

        String error = resolvedStatus != null
                ? resolvedStatus.getReasonPhrase()
                : "HTTP Error";

        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                statusCode.value(),
                error,
                message,
                path,
                validationErrors
        );

        return ResponseEntity.status(statusCode).body(errorResponse);
    }
}
