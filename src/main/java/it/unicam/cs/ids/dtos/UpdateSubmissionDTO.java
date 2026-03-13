package it.unicam.cs.ids.dtos;

import java.time.LocalDateTime;

public record UpdateSubmissionDTO(
        Long submissionId,
        String projectUrl,
        String description,
        LocalDateTime submissionDate
) {
}