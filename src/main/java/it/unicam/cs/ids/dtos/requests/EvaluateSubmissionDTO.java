package it.unicam.cs.ids.dtos.requests;

public record EvaluateSubmissionDTO(
        Long submissionId,
        Integer score,
        String feedback
) {
}