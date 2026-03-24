package it.unicam.cs.ids.validators;

import it.unicam.cs.ids.dtos.requests.EvaluateSubmissionDTO;
import it.unicam.cs.ids.validators.abstractions.Validator;

public class EvaluateSubmissionRequestValidator implements Validator<EvaluateSubmissionDTO> {
    @Override
    public void validate(EvaluateSubmissionDTO entity) {
        if (entity.submissionId() == null || entity.submissionId() <= 0) {
            throw new IllegalArgumentException("L'ID della sottomissione non e' valido o e' mancante.");
        }
        if (entity.judgeId()== null || entity.judgeId() <= 0) {
            throw new IllegalArgumentException("L'ID del giudice non e' valido o e' mancante.");
        }
        if (entity.score() < 0 || entity.score() > 10) {
            throw new IllegalArgumentException("Il punteggio deve essere compreso tra 0 e 10");
        }
        if (entity.feedback().isEmpty()) {
            throw new IllegalArgumentException("Devi aggiungere un feedback");
        }
    }
}