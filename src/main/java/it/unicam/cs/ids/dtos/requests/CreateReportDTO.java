package it.unicam.cs.ids.dtos.requests;

public record CreateReportDTO(
        Long mentorId,      // TODO: In futuro lo prenderemo dal token JWT
        Long teamId,
        Long hackathonId,
        String description
) {
}