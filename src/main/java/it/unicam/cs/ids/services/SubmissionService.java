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

import java.time.LocalDateTime;
import java.util.Optional;

public class SubmissionService implements ISubmissionService {

    private final ITeamRepository teamRepository;
    private final IHackathonRepository hackathonRepository;
    private final ISubmissionRepository submissionRepository;
    private final Validator<CreateSubmissionDTO> submissionValidator;


    public SubmissionService(ITeamRepository teamRepository, IHackathonRepository hackathonRepository, ISubmissionRepository submissionRepository, Validator<CreateSubmissionDTO> submissionValidator) {
        this.teamRepository = teamRepository;
        this.hackathonRepository = hackathonRepository;
        this.submissionRepository = submissionRepository;
        this.submissionValidator = submissionValidator;
    }

    @Override
    public Submission addSubmission(CreateSubmissionDTO request) {
        submissionValidator.validate(request);

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
        if (LocalDateTime.now().isAfter(hackathon.getSubmitDeadline())) {
            throw new IllegalStateException("The submission deadline has already passed.");
        }

        // Cerchiamo se esiste già una sottomissione.
        // Se la lista è null, creiamo uno stream vuoto al volo.
        Optional<Submission> sub = (team.getSubmissions() == null) ? Optional.empty() :
                team.getSubmissions().stream()
                        .filter(s -> s.getHackathon().getId().equals(hackathon.getId()))
                        .findFirst();

        if (sub.isEmpty()) {
            // Se non esiste, creiamo una nuova sottomissione
            Submission newSubmission = new Submission();
            newSubmission.setTeam(team);
            newSubmission.setHackathon(hackathon);
            newSubmission.setProjectUrl(request.projectUrl());
            newSubmission.setDescription(request.description());
            newSubmission.setSubmissionDate(LocalDateTime.now());

            // 1. Salviamo nel database per generare l'ID
            Submission savedSubmission = submissionRepository.create(newSubmission);

            // 2. SINCRONIZZAZIONE DELLA RELAZIONE BIDIREZIONALE!
            // Aggiorniamo la lista in memoria del Team, altrimenti alla
            // prossima chiamata il team sembrerà non avere sottomissioni.
            team.getSubmissions().add(savedSubmission);

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