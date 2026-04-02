package it.unicam.cs.ids.dtos.requests;

public record CreateSupportRequestDTO(
        Long hackathonId,
        String message
) {
}