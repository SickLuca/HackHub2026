package it.unicam.cs.ids.dtos.requests;

import java.time.LocalDateTime;

public record CreateInvitationDTO(

         String description,
         Long fromTeamId,
         Long toUserId,
         LocalDateTime creationDate
) {
}
