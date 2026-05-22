package it.unicam.cs.ids.dtos.requests;

import jakarta.validation.constraints.*;

public record EvaluateSubmissionDTO(
        @NotNull(message = "The submission ID is required")
        @Positive(message = "The ID must be a valid positive number")
        Long submissionId,

        @NotNull(message = "You must provide a score for the submission")
        @Min(value = 1, message = "The score must be greater than or equal to 1")
        @Max(value = 10, message = "The score must be less than or equal to 10")
        Integer score,

        @NotBlank(message = "You must provide feedback for the submission")
        String feedback
) {
}