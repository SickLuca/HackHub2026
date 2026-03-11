package it.unicam.cs.ids.validators;

import it.unicam.cs.ids.dtos.CreateHackathonDTO;
import it.unicam.cs.ids.validators.abstractions.Validator;

public class CreateHackathonValidator implements Validator<CreateHackathonDTO> {

    @Override
    public void validate(CreateHackathonDTO hackathon) {
        if (hackathon == null) {
            throw new IllegalArgumentException("L'hackathon non puo' essere nullo.");
        }
        if (hackathon.startDate().isAfter(hackathon.endDate())) {
            throw new IllegalArgumentException("La data di inizio non puo' essere successiva alla data di fine.");
        }
        if (hackathon.maxDimensionOfTeam() < 1) {
            throw new IllegalArgumentException("La dimensione massima del team deve essere almeno 1.");
        }
        if (hackathon.cashPrize() < 0) {
            throw new IllegalArgumentException("Il premio in denaro non puo' essere negativo.");
        }
    }
}