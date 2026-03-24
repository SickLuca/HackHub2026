package it.unicam.cs.ids.dtos.responses;

import java.time.LocalDateTime;

public record SupportRequestResponseDTO(
        Long id,
        String teamName,
        String hackathonName,
        String message,
        String status,
        LocalDateTime createdAt
) {
}