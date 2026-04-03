package it.unicam.cs.ids.dtos.requests;

public record PaymentRequestDTO(
        Long teamId,
        Double amount
) {
}