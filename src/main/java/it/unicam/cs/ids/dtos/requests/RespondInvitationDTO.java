package it.unicam.cs.ids.dtos.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RespondInvitationDTO(
        @NotNull(message = "L'ID dell'invito è obbligatorio")
        @Positive(message = "L'ID dell'utente deve essere un numero positivo valido")
        Long invitationId,

        @NotNull(message = "Devi specificare se accetti o rifiuti l'invito")
        Boolean accept
) {
}
