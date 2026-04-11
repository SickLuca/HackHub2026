package it.unicam.cs.ids.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


public record CreateInvitationDTO(

         @NotBlank(message = "La descrizione è obbligatoria")
         String description,

         @NotNull(message = "L'ID dell'utente da invitare è obbligatorio")
         @Positive(message = "L'ID dell'utente deve essere un numero positivo valido")
         Long toUserId
) {
}
