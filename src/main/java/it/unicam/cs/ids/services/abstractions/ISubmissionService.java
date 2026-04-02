package it.unicam.cs.ids.services.abstractions;

import it.unicam.cs.ids.dtos.requests.CreateSubmissionDTO;
import it.unicam.cs.ids.dtos.requests.EvaluateSubmissionDTO;
import it.unicam.cs.ids.dtos.responses.SubmissionResponseDTO;
import it.unicam.cs.ids.dtos.requests.UpdateSubmissionDTO;
import java.util.List;

public interface ISubmissionService {
    SubmissionResponseDTO addSubmission(CreateSubmissionDTO submission, Long userId);

    SubmissionResponseDTO updateSubmission(UpdateSubmissionDTO submission, Long userId);
    SubmissionResponseDTO evaluateSubmission(EvaluateSubmissionDTO request, Long judgeId);

    List<SubmissionResponseDTO> getSubmissionsByHackathon(Long hackathonId, Long staffId);
    SubmissionResponseDTO getSubmissionDetails(Long submissionId, Long staffId);
}