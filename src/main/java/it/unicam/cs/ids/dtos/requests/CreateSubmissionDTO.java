package it.unicam.cs.ids.dtos.requests;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateSubmissionDTO(
        @NotNull(message = "The hackathon ID is required")
        @Positive(message = "The ID must be a valid positive number")
        Long hackathonId,

        @NotBlank(message = "You must provide a URL for the submission")
        String projectUrl,

        @NotBlank(message = "You must provide a description for the submission")
        String description
) {
}