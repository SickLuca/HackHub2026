package it.unicam.cs.ids.dtos.requests;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

public record CreateHackathonDTO(

        @NotBlank(message = "Il nome dell'hackathon è obbligatorio")
        @Size(min = 3, max = 100, message = "Il nome deve avere tra 3 e 100 caratteri")
        String name,

        @NotNull(message = "La data di inizio non può essere nulla")
        @Future(message = "La data di inizio deve essere nel futuro")
        LocalDateTime startDate,

        @NotNull(message = "La scadenza delle iscrizioni non può essere nulla")
        @Future(message = "La scadenza delle iscrizioni deve essere nel futuro")
        LocalDateTime registrationDeadline,

        @NotNull(message = "La scadenza delle consegne non può essere nulla")
        @Future(message = "La scadenza delle consegne deve essere nel futuro")
        LocalDateTime submitDeadline,

        @NotBlank(message = "Il regolamento è obbligatorio")
        String regulation,

        @PositiveOrZero(message = "Il premio in denaro non può essere negativo")
        Double cashPrize,

        @NotBlank(message = "Il luogo è obbligatorio")
        String location,

        @Min(value = 1, message = "La dimensione massima del team deve essere almeno 1")
        Integer maxDimensionOfTeam,

        @NotNull(message = "Devi assegnare un giudice")
        Long judgeId,

        @NotEmpty(message = "Devi assegnare almeno un mentore")
        List<Long> mentorsIdS
) {

    /**
     * Questo metodo viene invocato automaticamente da Spring Validation.
     * Controlla la coerenza cronologica delle date.
     */
    @AssertTrue(message = "Errore di cronologia: la registrazione deve finire prima dell'inizio, e la consegna deve avvenire dopo l'inizio dell'hackathon.")
    public boolean isDatesValid() {
        // Se una delle date è null, restituiamo true per saltare questo controllo.
        // Ci penseranno le annotazioni @NotNull sopra a bloccare la richiesta,
        // evitando così un NullPointerException qui dentro.
        if (startDate == null || registrationDeadline == null || submitDeadline == null) {
            return true;
        }

        // Regola di business:
        // 1. La scadenza iscrizioni deve essere prima o uguale alla data di inizio
        // 2. La scadenza consegne deve essere dopo la data di inizio
        return (registrationDeadline.isBefore(startDate) || registrationDeadline.isEqual(startDate))
                && submitDeadline.isAfter(startDate) && submitDeadline.isAfter(registrationDeadline);
    }
}