package it.unicam.cs.ids.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateSubmissionDTO(
        @NotNull(message = "The submission ID is required")
        @Positive(message = "The ID must be a valid positive number")
        Long submissionId,

        @NotBlank(message = "You must provide a new URL for the submission")
        String projectUrl,

        @NotBlank(message = "You must provide a new description for the submission")
        String description
) {
}