package it.unicam.cs.ids.dtos;

import java.time.LocalDateTime;

public record CreateSubmissionDTO(
        Long teamId,
        Long hackathonId,
        String projectUrl,
        String description,
        LocalDateTime submissionDate
) {
}