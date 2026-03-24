package it.unicam.cs.ids.dtos.responses;

import it.unicam.cs.ids.models.utils.ReportStatus;

import java.time.LocalDateTime;

public record ReportResponseDTO(
        Long id,
        String mentorFullName,
        String teamName,
        String hackathonName,
        String description,
        ReportStatus status,
        LocalDateTime createdAt,
        String decisionNote
) {
}