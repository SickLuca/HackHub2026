package it.unicam.cs.ids.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateSupportRequestDTO(
        @NotNull(message = "Devi scegliere un hackathon")
        @Positive(message = "L'id del hackathon deve essere positivo")
        Long hackathonId,

        @NotBlank(message = "Devi inserire un messaggio")
        String message
) {
}