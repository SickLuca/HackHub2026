package it.unicam.cs.ids.dtos.requests;

public record SubscribeTeamDTO(
        Long teamId,
        Long hackathonId,
        Long userId
) {
}