package it.unicam.cs.ids.exceptions;

import java.time.LocalDateTime;

public record ApiErrorResponseDTO(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {}