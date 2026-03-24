package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.requests.CreateSubmissionDTO;
import it.unicam.cs.ids.dtos.requests.EvaluateSubmissionDTO;
import it.unicam.cs.ids.dtos.responses.SubmissionResponseDTO;
import it.unicam.cs.ids.dtos.requests.UpdateSubmissionDTO;
import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.models.Submission;
import it.unicam.cs.ids.models.Team;
import it.unicam.cs.ids.models.utils.HackathonStatus;
import it.unicam.cs.ids.models.utils.SubmissionStatus;
import it.unicam.cs.ids.services.abstractions.ISubmissionService;
import it.unicam.cs.ids.utils.unitOfWork.IUnitOfWork;
import it.unicam.cs.ids.validators.abstractions.Validator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class SubmissionService implements ISubmissionService {

    private final IUnitOfWork unitOfWork;
    private final Validator<CreateSubmissionDTO> createSubmissionValidator;
    private final Validator<EvaluateSubmissionDTO> evaluateSubmissionValidator;


    public SubmissionService(IUnitOfWork unitOfWork, Validator<CreateSubmissionDTO> submissionValidator, Validator<EvaluateSubmissionDTO> evaluateSubmissionValidator) {
        this.unitOfWork = unitOfWork;
        this.createSubmissionValidator = submissionValidator;
        this.evaluateSubmissionValidator = evaluateSubmissionValidator;
    }

    @Override
    public SubmissionResponseDTO addSubmission(CreateSubmissionDTO request) {
        createSubmissionValidator.validate(request);

        Team team = unitOfWork.getTeamRepository().getById(request.teamId());
        if (team == null) throw new IllegalArgumentException("Team not found.");

        Hackathon hackathon = unitOfWork.getHackathonRepository().getById(request.hackathonId());
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
            newSubmission.setStatus(SubmissionStatus.OPEN);
            newSubmission.setScore(0);
            newSubmission.setJudgeFeedback("");


            // 1. Salviamo nel database per generare l'ID
            Submission savedSubmission = unitOfWork.getSubmissionRepository().create(newSubmission);

            // 2. SINCRONIZZAZIONE DELLA RELAZIONE BIDIREZIONALE!
            // Aggiorniamo la lista in memoria del Team, altrimenti alla
            // prossima chiamata il team sembrerà non avere sottomissioni.
            team.getSubmissions().add(savedSubmission);
            hackathon.getSubmissions().add(savedSubmission);

            return mapToDTO(savedSubmission);
        } else {
            //TODO: oppure mappiamo la richiesta di creazione ad un update e ritorniamo l'update :)

            // Se esiste, informiamo che una sottomissione è già presente e che è possibile modificarla
            throw new IllegalStateException("A submission for this Hackathon already exists.");
        }
    }

    @Override
    public SubmissionResponseDTO updateSubmission(UpdateSubmissionDTO request) {
        Submission submission = unitOfWork.getSubmissionRepository().getById(request.submissionId());
        if (submission == null) {
            throw new IllegalArgumentException("Submission not found.");
        }
        if (submission.getHackathon().getSubmitDeadline().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("The submission deadline has already passed.");
        }
        submission.setProjectUrl(request.projectUrl());
        submission.setDescription(request.description());
        submission.setSubmissionDate(LocalDateTime.now());
        unitOfWork.getSubmissionRepository().update(submission);

        return mapToDTO(submission);
    }

    @Override
    public SubmissionResponseDTO evaluateSubmission(EvaluateSubmissionDTO request) {
        evaluateSubmissionValidator.validate(request);

        // 1. Recupero la sottomissione
        Submission submission = unitOfWork.getSubmissionRepository().getById(request.submissionId());
        if (submission == null) {
            throw new IllegalArgumentException("Sottomissione non trovata");
        }

        Hackathon hackathon = submission.getHackathon();

        // 2. Controllo di sicurezza: chi valuta è davvero il giudice di questo Hackathon?
        if (!hackathon.getJudge().getId().equals(request.judgeId())) {
            throw new SecurityException("Non sei il giudice assegnato a questo Hackathon");
        }

        if (hackathon.getStatus() != HackathonStatus.IN_PROGRESS) {
            throw new IllegalStateException("L'hackathon non è attualmente in fase di valutazione");
        }

        //Solo closed perchè non puoi gestirla se aperta o valutata
        if (submission.getStatus() != SubmissionStatus.CLOSED ) {
            throw new IllegalStateException("Non puoi gestire questa sottomissione");
        }

        // 5. Aggiornamento dell'entità
        submission.setScore(request.score());
        submission.setJudgeFeedback(request.feedback());

        unitOfWork.getSubmissionRepository().update(submission);

        return mapToDTO(submission);
    }

    @Override
    public List<SubmissionResponseDTO> getSubmissionsByHackathon(Long hackathonId, Long staffId) {
        Hackathon hackathon = unitOfWork.getHackathonRepository().getById(hackathonId);
        if (hackathon == null) {
            throw new IllegalArgumentException("Hackathon non trovato.");
        }

        // Controllo Sicurezza: lo staff fa parte di questo hackathon?
        if (!isStaffAssignedToHackathon(hackathon, staffId)) {
            throw new SecurityException("Non sei autorizzato a visualizzare le sottomissioni di questo hackathon.");
        }

        List<Submission> submissions = unitOfWork.getSubmissionRepository().getByHackathon(hackathonId);

        return submissions.stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public SubmissionResponseDTO getSubmissionDetails(Long submissionId, Long staffId) {
        Submission submission = unitOfWork.getSubmissionRepository().getById(submissionId);
        if (submission == null) {
            throw new IllegalArgumentException("Sottomissione non trovata.");
        }

        // Controllo Sicurezza: lo staff fa parte dell'hackathon a cui appartiene questa sottomissione?
        if (!isStaffAssignedToHackathon(submission.getHackathon(), staffId)) {
            throw new SecurityException("Non sei autorizzato a visualizzare i dettagli di questa sottomissione.");
        }

        return mapToDTO(submission);
    }

    // Metodo helper privato per centralizzare il controllo degli accessi
    private boolean isStaffAssignedToHackathon(Hackathon hackathon, Long staffId) {
        // È l'organizzatore?
        if (hackathon.getOrganizer().getId().equals(staffId)) return true;

        // È il giudice?
        if (hackathon.getJudge().getId().equals(staffId)) return true;

        // È uno dei mentori?
        return hackathon.getMentors().stream().anyMatch(m -> m.getId().equals(staffId));
    }


    private SubmissionResponseDTO mapToDTO(Submission submission) {
        return new SubmissionResponseDTO(
                submission.getId(),
                submission.getTeam().getName(),
                submission.getHackathon().getName(),
                submission.getProjectUrl(),
                submission.getDescription(),
                submission.getSubmissionDate(),
                submission.getStatus().toString(),
                submission.getScore(),
                submission.getJudgeFeedback()
        );
    }
}