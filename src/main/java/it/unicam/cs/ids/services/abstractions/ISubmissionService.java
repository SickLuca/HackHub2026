package it.unicam.cs.ids.services.abstractions;

import it.unicam.cs.ids.dtos.CreateSubmissionDTO;
import it.unicam.cs.ids.dtos.SubmissionResponseDTO;
import it.unicam.cs.ids.dtos.UpdateSubmissionDTO;
import it.unicam.cs.ids.models.Submission;

import java.util.List;

public interface ISubmissionService {
    SubmissionResponseDTO addSubmission(CreateSubmissionDTO submission);

    SubmissionResponseDTO updateSubmission(UpdateSubmissionDTO submission);

    //List<Submission> getSubmissionsForHackathon(Long hackathonId);
}