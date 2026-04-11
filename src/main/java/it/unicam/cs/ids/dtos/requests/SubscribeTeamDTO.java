package it.unicam.cs.ids.dtos.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SubscribeTeamDTO(
        @NotNull(message = "Devi scegliere un hackathon")
        @Positive(message = "L'id del hackathon deve essere positivo")
        Long hackathonId
) {
}