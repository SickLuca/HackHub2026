package it.unicam.cs.ids.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateReportDTO(
        @NotNull(message = "The ID of the team to report is required")
        @Positive(message = "The ID must be a valid positive number")
        Long teamId,

        @NotNull(message = "The ID of the referenced hackathon is required")
        @Positive(message = "The ID must be a valid positive number")
        Long hackathonId,

        @NotBlank(message = "A reason for the report must be provided")
        String description
) {
}