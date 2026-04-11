package it.unicam.cs.ids.dtos.requests;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record ScheduleCallDTO(
        @NotNull(message = "L'ID della richiesta di supporto è obbligatorio")
        @Positive(message = "L'ID deve essere un numero positivo")
        Long supportRequestId,

        @NotNull(message = "Devi inserire una data per la call")
        @Future (message = "La data deve essere nel futuro")
        LocalDateTime callDate
) {
}