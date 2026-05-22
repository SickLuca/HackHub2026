package it.unicam.cs.ids.dtos.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SubscribeTeamDTO(
        @NotNull(message = "You must choose a hackathon")
        @Positive(message = "The hackathon ID must be positive")
        Long hackathonId
) {
}