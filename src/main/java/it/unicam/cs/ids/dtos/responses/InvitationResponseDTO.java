package it.unicam.cs.ids.dtos.responses;

import it.unicam.cs.ids.models.utils.InvitationStatus;

import java.time.LocalDateTime;

public record InvitationResponseDTO(
        String forUserName,
        String fromTeamName,
        InvitationStatus status,
        LocalDateTime creationDate
) {
}
