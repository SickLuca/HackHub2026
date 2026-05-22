package it.unicam.cs.ids.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Handles custom exceptions (Resource Not Found)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage(), request);
    }

    // 2. Handles exceptions for business rule violations
    @ExceptionHandler(RuleViolationException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleBusinessRulesExceptions(RuleViolationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "Business Rule Violation", ex.getMessage(), request);
    }

    // 3. Internal permissions and authorization (403 Forbidden)
    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleUnauthorizedAction(UnauthorizedActionException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Unauthorized Action", ex.getMessage(), request);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errorMessage = ex.getBindingResult().getFieldErrors().getFirst().getDefaultMessage();
        return buildResponse(HttpStatus.BAD_REQUEST, "Input Validation Error", errorMessage, request);
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleInvalidInput(InvalidInputException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid Input", ex.getMessage(), request);
    }

    // --- Helper method to build the response cleanly ---
    private ResponseEntity<ApiErrorResponseDTO> buildResponse(HttpStatus status, String error, String message, HttpServletRequest request) {
        ApiErrorResponseDTO response = new ApiErrorResponseDTO(
                LocalDateTime.now(),
                status.value(),
                error,
                message,
                request.getRequestURI()
        );
        return new ResponseEntity<>(response, status);
    }
}
