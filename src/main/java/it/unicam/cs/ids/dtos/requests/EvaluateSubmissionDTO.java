package it.unicam.cs.ids.dtos.requests;

public record EvaluateSubmissionDTO(
        Long submissionId,
        Long judgeId,
        Integer score,
        String feedback
) {
}