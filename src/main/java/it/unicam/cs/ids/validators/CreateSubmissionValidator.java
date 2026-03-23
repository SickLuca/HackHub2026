package it.unicam.cs.ids.validators;

import it.unicam.cs.ids.dtos.requests.CreateSubmissionDTO;
import it.unicam.cs.ids.validators.abstractions.Validator;

public class CreateSubmissionValidator implements Validator<CreateSubmissionDTO> {
    @Override
    public void validate(CreateSubmissionDTO entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La richiesta di sottomissione non puo' essere nulla.");
        }

        if (entity.teamId() == null || entity.teamId() <= 0) {
            throw new IllegalArgumentException("L'ID del team non e' valido o e' mancante.");
        }

        if (entity.hackathonId() == null || entity.hackathonId() <= 0) {
            throw new IllegalArgumentException("L'ID dell'hackathon non e' valido o e' mancante.");
        }

        if (entity.projectUrl() == null || entity.projectUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("L'URL del progetto è obbligatorio e non puo' essere vuoto.");
        }

    }
}
