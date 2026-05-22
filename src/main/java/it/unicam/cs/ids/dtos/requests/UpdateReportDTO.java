package it.unicam.cs.ids.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateReportDTO (
        @NotNull(message = "The report ID is required")
        @Positive(message = "The ID must be a valid positive number")
        Long reportId,

        @NotBlank(message = "You must specify the decision for the report")
        String decisionNote
){
}