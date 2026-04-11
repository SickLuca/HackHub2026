package it.unicam.cs.ids.dtos.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddMentorDTO(
        @NotNull(message = "Devi scegliere un hackathon")
        @Positive(message = "L'id del hackathon deve essere positivo")
        Long hackathonId,

        @NotNull(message = "Devi scegliere un mentore")
        @Positive(message = "L'id del mentore deve essere positivo")
        Long mentorId
) {
}