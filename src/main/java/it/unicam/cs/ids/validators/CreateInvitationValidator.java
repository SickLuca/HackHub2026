package it.unicam.cs.ids.validators;

import it.unicam.cs.ids.dtos.requests.CreateInvitationDTO;
import it.unicam.cs.ids.validators.abstractions.Validator;

public class CreateInvitationValidator implements Validator<CreateInvitationDTO> {

    @Override
    public void validate(CreateInvitationDTO entity) {
        // Controllo che l'oggetto request non sia nullo
        if (entity == null) {
            throw new IllegalArgumentException("La richiesta di invito non può essere nulla.");
        }

        // Controllo validità dell'ID del team mittente
        if (entity.fromTeamId() == null || entity.fromTeamId() <= 0) {
            throw new IllegalArgumentException("L'ID del team mittente è obbligatorio e deve essere valido (> 0).");
        }

        // Controllo validità dell'ID dell'utente destinatario
        if (entity.toUserId() == null || entity.toUserId() <= 0) {
            throw new IllegalArgumentException("L'ID dell'utente da invitare è obbligatorio e deve essere valido (> 0).");
        }

        // Controllo opzionale sulla lunghezza della descrizione (se presente)
        if (entity.description() != null && entity.description().length() > 500) {
            throw new IllegalArgumentException("La descrizione dell'invito è troppo lunga (massimo 500 caratteri).");
        }
    }

}
