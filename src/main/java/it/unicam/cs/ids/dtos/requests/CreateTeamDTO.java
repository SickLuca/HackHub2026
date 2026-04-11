package it.unicam.cs.ids.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTeamDTO(
        @NotBlank(message = "Il nome del team è obbligatorio")
        @Size(min = 3, max = 40, message = "Il nome deve avere tra 3 e 100 caratteri")
        String name
) {
}
