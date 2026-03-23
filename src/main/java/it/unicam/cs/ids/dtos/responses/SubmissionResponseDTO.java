package it.unicam.cs.ids.dtos.responses;


import java.time.LocalDateTime;

public record SubmissionResponseDTO(
        Long id,
        String teamName,
        String hackathonName,
        String projectUrl,
        String description,
        LocalDateTime submissionDate
) {
}
