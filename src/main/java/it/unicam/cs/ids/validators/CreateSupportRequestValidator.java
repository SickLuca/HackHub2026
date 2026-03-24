package it.unicam.cs.ids.validators;

import it.unicam.cs.ids.dtos.requests.CreateSupportRequestDTO;
import it.unicam.cs.ids.validators.abstractions.Validator;

public class CreateSupportRequestValidator implements Validator<CreateSupportRequestDTO> {
    @Override
    public void validate(CreateSupportRequestDTO entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La richiesta di supporto non puo' essere nulla.");
        }
        if (entity.hackathonId() == null || entity.hackathonId() <= 0) {
            throw new IllegalArgumentException("L'ID dell'hackathon non e' valido o e' mancante.");
        }
        if (entity.teamId() == null || entity.teamId() <= 0) {
            throw new IllegalArgumentException("L'ID del team non e' valido o e' mancante.");
        }
        if (entity.message().isEmpty()) {
            throw new IllegalArgumentException("Devi inserire un messaggio.");
        }
        if (entity.message().length() > 500) {
            throw new IllegalArgumentException("Il messaggio non puo' superare i 500 caratteri.");
        }
    }
}