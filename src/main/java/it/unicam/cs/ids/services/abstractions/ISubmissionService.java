package it.unicam.cs.ids.services.abstractions;

import it.unicam.cs.ids.dtos.requests.CreateSubmissionDTO;
import it.unicam.cs.ids.dtos.responses.SubmissionResponseDTO;
import it.unicam.cs.ids.dtos.requests.UpdateSubmissionDTO;

public interface ISubmissionService {
    SubmissionResponseDTO addSubmission(CreateSubmissionDTO submission);

    SubmissionResponseDTO updateSubmission(UpdateSubmissionDTO submission);

    //List<Submission> getSubmissionsForHackathon(Long hackathonId);
}