package it.unicam.cs.ids.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateReportDTO(
        @NotNull(message = "L'ID del team da segnalare è obbligatorio")
        @Positive(message = "L'ID  deve essere un numero positivo valido")
        Long teamId,

        @NotNull(message = "L'ID dell'hackathon di riferimento è obbligatorio")
        @Positive(message = "L'ID  deve essere un numero positivo valido")
        Long hackathonId,

        @NotBlank(message = "Deve essere presente una motivazione per la segnalazione")
        String description
) {
}