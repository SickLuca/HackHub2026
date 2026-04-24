package it.unicam.cs.ids.dtos.responses;

import it.unicam.cs.ids.models.utils.HackathonStatus;

import java.time.LocalDateTime;

public record HackathonPublicResponseDTO(
        String name,
        LocalDateTime startDate,
        HackathonStatus status,
        String winnerName
) {
}
