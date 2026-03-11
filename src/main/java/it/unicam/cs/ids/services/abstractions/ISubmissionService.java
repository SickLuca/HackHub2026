package it.unicam.cs.ids.services.abstractions;

import it.unicam.cs.ids.dtos.CreateSubmissionDTO;
import it.unicam.cs.ids.models.Submission;

public interface ISubmissionService {
    Submission addSubmission(CreateSubmissionDTO submission);
}
