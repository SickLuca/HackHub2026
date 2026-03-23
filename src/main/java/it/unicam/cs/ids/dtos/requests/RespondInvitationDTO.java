package it.unicam.cs.ids.dtos.requests;

public record RespondInvitationDTO(
        Long invitationId,
        Boolean accept
) {
}
