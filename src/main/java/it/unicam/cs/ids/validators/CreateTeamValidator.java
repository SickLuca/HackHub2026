package it.unicam.cs.ids.validators;

import it.unicam.cs.ids.dtos.requests.CreateTeamDTO;
import it.unicam.cs.ids.validators.abstractions.Validator;

public class CreateTeamValidator implements Validator<CreateTeamDTO> {

    @Override
    public void validate(CreateTeamDTO team) {
        if (team == null) {
            throw new IllegalArgumentException("Il team non puo' essere nullo.");
        }
        // Aggiunto anche il controllo sui null per evitare NullPointerException
        if (team.name() == null || team.name().length() < 5) {
            throw new IllegalArgumentException("Il nome del team deve contenere almeno 5 caratteri.");
        }
    }
}