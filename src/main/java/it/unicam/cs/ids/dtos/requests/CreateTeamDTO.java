package it.unicam.cs.ids.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTeamDTO(
        @NotBlank(message = "The team name is required")
        @Size(min = 3, max = 40, message = "The name must be between 3 and 40 characters")
        String name
) {
}
