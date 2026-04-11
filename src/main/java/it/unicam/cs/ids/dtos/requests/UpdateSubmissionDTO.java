package it.unicam.cs.ids.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateSubmissionDTO(
        @NotNull(message = "L'ID della sottomissione è obbligatorio")
        @Positive(message = "L'ID deve essere un numero positivo valido")
        Long submissionId,

        @NotBlank(message = "Devi inserire un nuovo url per la sottomissione")
        String projectUrl,

        @NotBlank(message = "Devi inserire una nuova descrizione per la sottomissione")
        String description
) {
}