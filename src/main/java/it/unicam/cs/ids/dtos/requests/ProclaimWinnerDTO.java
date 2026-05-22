package it.unicam.cs.ids.dtos.requests;

import it.unicam.cs.ids.models.utils.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProclaimWinnerDTO(
        @NotNull(message = "You must choose a hackathon")
        @Positive(message = "The hackathon ID must be positive")
        Long hackathonId,

        @NotNull(message = "You must choose a team")
        @Positive(message = "The team ID must be positive")
        Long winningTeamId,

        @NotNull(message = "The payment method is required to disburse the prize")
        PaymentMethod paymentMethod
) {

}