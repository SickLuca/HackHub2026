package it.unicam.cs.ids.dtos.requests;

public record CreateSupportRequestDTO(
        Long teamId,
        Long hackathonId,
        String message
) {
}