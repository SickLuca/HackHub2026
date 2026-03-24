package it.unicam.cs.ids.controllers;

import it.unicam.cs.ids.dtos.requests.CreateSubmissionDTO;
import it.unicam.cs.ids.dtos.requests.EvaluateSubmissionDTO;
import it.unicam.cs.ids.dtos.responses.SubmissionResponseDTO;
import it.unicam.cs.ids.dtos.requests.UpdateSubmissionDTO;
import it.unicam.cs.ids.services.abstractions.ISubmissionService;
import java.util.List;

public class SubmissionController {

    private final ISubmissionService submissionService;

    // Costruttore per l'iniezione delle dipendenze (uguale al TeamController)
    public SubmissionController(ISubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    // Metodo per il Membro del Team per inviare il progetto la prima volta
    public SubmissionResponseDTO submitProject(CreateSubmissionDTO request) {
        return submissionService.addSubmission(request);
    }

    //Metodo per il Membro del Team per aggiornare il progetto prima della scadenza
    public SubmissionResponseDTO updateSubmission(UpdateSubmissionDTO request) {
        return submissionService.updateSubmission(request);
    }

    //Metodo per il Giudice per valutare una sottomissione
    public SubmissionResponseDTO evaluateSubmission(EvaluateSubmissionDTO request) {
        return submissionService.evaluateSubmission(request);
    }

    public SubmissionResponseDTO getSubmissionDetails(Long submissionId, Long staffId) {
        return submissionService.getSubmissionDetails(submissionId, staffId);
    }

    public List<SubmissionResponseDTO> getSubmissionsByHackathon(Long hackathonId, Long staffId) {
        return submissionService.getSubmissionsByHackathon(hackathonId, staffId);
    }
}