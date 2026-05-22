package it.unicam.cs.ids.dtos.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RespondInvitationDTO(
        @NotNull(message = "The invitation ID is required")
        @Positive(message = "The ID must be a valid positive number")
        Long invitationId,

        @NotNull(message = "You must specify whether you accept or reject the invitation")
        Boolean accept
) {
}
