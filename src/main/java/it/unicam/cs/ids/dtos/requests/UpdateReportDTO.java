package it.unicam.cs.ids.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateReportDTO (
        @NotNull(message = "L'ID della segnalazione è obbligatorio")
        @Positive(message = "L'ID deve essere un numero positivo valido")
        Long reportId,

        @NotBlank(message = "Devi specificare la decisione per la segnalazione")
        String decisionNote
){
}