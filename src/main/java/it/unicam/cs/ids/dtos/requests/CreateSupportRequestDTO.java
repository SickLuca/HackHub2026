package it.unicam.cs.ids.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateSupportRequestDTO(
        @NotNull(message = "You must choose a hackathon")
        @Positive(message = "The hackathon ID must be positive")
        Long hackathonId,

        @NotBlank(message = "You must provide a message")
        String message
) {
}