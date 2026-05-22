package it.unicam.cs.ids.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


public record CreateInvitationDTO(

         @NotBlank(message = "The description is required")
         String description,

         @NotNull(message = "The ID of the user to invite is required")
         @Positive(message = "The user ID must be a valid positive number")
         Long toUserId
) {
}
