package it.unicam.cs.ids.dtos.requests;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateSubmissionDTO(
        @NotNull(message = "L'ID dell'hackathon è obbligatorio")
        @Positive(message = "L'ID  deve essere un numero positivo valido")
        Long hackathonId,

        @NotBlank(message = "Devi inserire un url per la sottomissione")
        String projectUrl,

        @NotBlank(message = "Devi inserire una descrizione per la sottomissione")
        String description
) {
}