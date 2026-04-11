package it.unicam.cs.ids.dtos.requests;

import jakarta.validation.constraints.*;

public record EvaluateSubmissionDTO(
        @NotNull(message = "L'ID della sottomissione è obbligatorio")
        @Positive(message = "L'ID  deve essere un numero positivo valido")
        Long submissionId,

        @NotNull(message = "Devi inserire un voto per la sottomissione")
        @Min(value = 1, message = "Il voto deve essere maggiore o uguale a 0")
        @Max(value = 10, message = "Il voto deve essere minore o uguale a 10")
        Integer score,

        @NotBlank(message = "Devi inserire un feedback per la sottomissione")
        String feedback
) {
}