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

    // 1. Gestisce le eccezioni custom (Resource Not Found)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Risorsa Non Trovata", ex.getMessage(), request);
    }

    // 2. Gestisce le vecchie IllegalStateException che hai già nei Service (oppure la nuova RuleViolationException)
    @ExceptionHandler({IllegalStateException.class, RuleViolationException.class})
    public ResponseEntity<ApiErrorResponseDTO> handleBusinessRulesExceptions(RuntimeException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "Violazione Regola di Business", ex.getMessage(), request);
    }

    // 3. Permessi e Autorizzazioni interne (403 Forbidden)
    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleUnauthorizedAction(UnauthorizedActionException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Azione Non Autorizzata", ex.getMessage(), request);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errorMessage = ex.getBindingResult().getFieldErrors().getFirst().getDefaultMessage();
        return buildResponse(HttpStatus.BAD_REQUEST, "Errore di Validazione Input", errorMessage, request);
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleInvalidInput(InvalidInputException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Input Non Valido", ex.getMessage(), request);
    }

    // --- Helper Method per costruire la risposta in modo pulito ---
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
