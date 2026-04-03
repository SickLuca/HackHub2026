package it.unicam.cs.ids.dtos.requests;

import it.unicam.cs.ids.models.utils.PaymentMethod;

public record ProclaimWinnerDTO(
        Long hackathonId,
        Long winningTeamId,
        PaymentMethod paymentMethod
) {

}