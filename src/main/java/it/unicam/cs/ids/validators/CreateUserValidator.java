package it.unicam.cs.ids.validators;

import it.unicam.cs.ids.dtos.requests.RegisterRequestDTO;
import it.unicam.cs.ids.validators.abstractions.Validator;
import org.springframework.stereotype.Component;

@Component
public class CreateUserValidator implements Validator<RegisterRequestDTO> {
    @Override
    public void validate(RegisterRequestDTO entity) {
        if (entity == null) {
            throw new IllegalArgumentException("L'utente non puo' essere nullo.");
        }

        if (entity.email() == null || entity.email().isEmpty()) {
            throw new IllegalArgumentException("L'email non puo' essere vuota.");
        }

        if (entity.password() == null || entity.password().isEmpty()) {
            throw new IllegalArgumentException("La password non puo' essere vuota.");
        }

        if (entity.name() == null || entity.name().isEmpty()) {
            throw new IllegalArgumentException("Il nome non puo' essere vuoto.");
        }

        if (entity.surname() == null || entity.surname().isEmpty()) {
            throw new IllegalArgumentException("Il cognome non puo' essere vuoto.");
        }

    }
}
