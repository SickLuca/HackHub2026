package it.unicam.cs.ids.dtos.requests;

import it.unicam.cs.ids.models.utils.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProclaimWinnerDTO(
        @NotNull(message = "Devi scegliere un hackathon")
        @Positive(message = "L'id del hackathon deve essere positivo")
        Long hackathonId,

        @NotNull(message = "Devi scegliere un team")
        @Positive(message = "L'id del team deve essere positivo")
        Long winningTeamId,

        @NotNull(message = "Il metodo di pagamento è obbligatorio per erogare il premio")
        PaymentMethod paymentMethod
) {

}