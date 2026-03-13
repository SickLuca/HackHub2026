package it.unicam.cs.ids.services.abstractions;

import it.unicam.cs.ids.dtos.CreateSubmissionDTO;
import it.unicam.cs.ids.dtos.UpdateSubmissionDTO;
import it.unicam.cs.ids.models.Submission;

import java.util.List;

public interface ISubmissionService {
    Submission addSubmission(CreateSubmissionDTO submission);

    Submission updateSubmission(UpdateSubmissionDTO submission);

    //List<Submission> getSubmissionsForHackathon(Long hackathonId);
}