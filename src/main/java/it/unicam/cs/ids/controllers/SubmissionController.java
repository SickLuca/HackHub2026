package it.unicam.cs.ids.controllers;

import it.unicam.cs.ids.dtos.CreateSubmissionDTO;
// import it.unicam.cs.ids.dtos.UpdateSubmissionDTO;
import it.unicam.cs.ids.models.Submission;
import it.unicam.cs.ids.services.abstractions.ISubmissionService;

import java.util.List;

public class SubmissionController {

    private final ISubmissionService submissionService;

    // Costruttore per l'iniezione delle dipendenze (uguale al TeamController)
    public SubmissionController(ISubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    // Metodo per il Membro del Team per inviare il progetto la prima volta
    public Submission submitProject(CreateSubmissionDTO request) {
        return submissionService.createSubmission(request);
    }

    Metodo per il Membro del Team per aggiornare il progetto prima della scadenza
   public Submission updateSubmission(UpdateSubmissionDTO request) {
      return submissionService.updateSubmission(request);
   }

    // Metodo utile per il Giudice (o lo Staff) per recuperare tutte le sottomissioni di un hackathon
    public List<Submission> getSubmissionsByHackathon(Long hackathonId) {
        return submissionService.getSubmissionsForHackathon(hackathonId);
    }
}