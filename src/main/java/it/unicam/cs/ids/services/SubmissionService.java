package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.CreateSubmissionDTO;
import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.models.Submission;
import it.unicam.cs.ids.models.Team;
import it.unicam.cs.ids.repositories.abstractions.IDefaultUserRepository;
import it.unicam.cs.ids.repositories.abstractions.IHackathonRepository;
import it.unicam.cs.ids.repositories.abstractions.ISubmissionRepository;
import it.unicam.cs.ids.repositories.abstractions.ITeamRepository;
import it.unicam.cs.ids.services.abstractions.ISubmissionService;
import it.unicam.cs.ids.validators.abstractions.Validator;

import java.util.Optional;

public class SubmissionService implements ISubmissionService {

    private final ITeamRepository teamRepository;
    private final IHackathonRepository hackathonRepository;
    private final ISubmissionRepository submissionRepository;


    public SubmissionService(ITeamRepository teamRepository, IHackathonRepository hackathonRepository, ISubmissionRepository submissionRepository) {
        this.teamRepository = teamRepository;
        this.hackathonRepository = hackathonRepository;
        this.submissionRepository = submissionRepository;
    }

    @Override
    public Submission addSubmission(CreateSubmissionDTO request) {
        Team team = teamRepository.getById(request.teamId());
        if (team == null) throw new IllegalArgumentException("Team not found.");

        Hackathon hackathon = hackathonRepository.getById(request.hackathonId());
        if (hackathon == null) throw new IllegalArgumentException("Hackathon not found.");

        // Controllo 1: Il team è iscritto a QUESTO hackathon?
        if (team.getSubscribedHackathon() == null || !team.getSubscribedHackathon().getId().equals(hackathon.getId())) {
            throw new IllegalStateException("The team is not subscribed to this Hackathon.");
        }

        // Controllo 2: La scadenza per le consegne è passata?
        // È importante verificare la data di scadenza esatta come da specifiche
        if (java.time.LocalDateTime.now().isAfter(hackathon.getSubmitDeadline())) {
            throw new IllegalStateException("The submission deadline has already passed.");
        }

        // Cerchiamo se esiste già una sottomissione tramite gli Stream sulla lista del Team
        // Nota: Assicurati che team.getSubmissions() non sia null, altrimenti avrai un NullPointerException.
        // Se hai inizializzato la lista nella classe Team (es. private List<Submission> submissions = new ArrayList<>();) sei a posto!
        Optional<Submission> sub = team.getSubmissions().stream()
                .filter(s -> s.getHackathon().getId().equals(hackathon.getId()))
                .findFirst();

        if (sub.isEmpty()) {
            // Se non esiste, creiamo una nuova sottomissione
            Submission newSubmission = new Submission();
            newSubmission.setTeam(team);
            newSubmission.setHackathon(hackathon);
            newSubmission.setProjectUrl(request.projectUrl());
            newSubmission.setDescription(request.description());
            newSubmission.setSubmissionDate(java.time.LocalDateTime.now());

            return submissionRepository.create(newSubmission);
        } else {
            // Se esiste, ESTRAIAMO l'oggetto dall'Optional e lo AGGIORNIAMO
            Submission existingSubmission = sub.get(); // <--- LA SOLUZIONE È QUI

            existingSubmission.setProjectUrl(request.projectUrl());
            existingSubmission.setDescription(request.description());
            existingSubmission.setSubmissionDate(java.time.LocalDateTime.now());

            return submissionRepository.update(existingSubmission);
        }
    }
}