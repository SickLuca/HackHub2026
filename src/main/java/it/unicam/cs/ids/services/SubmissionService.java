package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.requests.CreateSubmissionDTO;
import it.unicam.cs.ids.dtos.requests.EvaluateSubmissionDTO;
import it.unicam.cs.ids.dtos.responses.SubmissionResponseDTO;
import it.unicam.cs.ids.dtos.requests.UpdateSubmissionDTO;
import it.unicam.cs.ids.exceptions.ResourceNotFoundException;
import it.unicam.cs.ids.exceptions.RuleViolationException;
import it.unicam.cs.ids.exceptions.UnauthorizedActionException;
import it.unicam.cs.ids.models.DefaultUser;
import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.models.Submission;
import it.unicam.cs.ids.models.Team;
import it.unicam.cs.ids.models.utils.HackathonStatus;
import it.unicam.cs.ids.models.utils.SubmissionStatus;
import it.unicam.cs.ids.services.abstractions.ISubmissionService;
import it.unicam.cs.ids.utils.unitOfWork.IUnitOfWork;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servizio per la gestione delle submission (consegne dei progetti).
 * <p>
 * Contiene la logica per permettere ai team di presentare i propri progetti
 * entro la scadenza stabilita (deadline). Permette anche l'aggiornamento
 * delle submission e la valutazione da parte dei giudici (assegnazione
 * di un punteggio e di un feedback).
 * </p>
 */
@Service
@Transactional
public class SubmissionService implements ISubmissionService {

    private final IUnitOfWork unitOfWork;


    public SubmissionService(IUnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    @Override
    public SubmissionResponseDTO addSubmission(CreateSubmissionDTO request, Long userId) {
        // 1. Recupero Utente e Team in modo sicuro!
        DefaultUser user = unitOfWork.getDefaultUserRepository().findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));

        Team team = user.getTeam();
        if (team == null) {
            throw new RuleViolationException("Devi appartenere a un team per poter sottomettere un progetto.");
        }

        Hackathon hackathon = unitOfWork.getHackathonRepository().findById(request.hackathonId()).orElse(null);
        if (hackathon == null) throw new ResourceNotFoundException("Hackathon not found.");

        // Controllo 1: Il team è iscritto a QUESTO hackathon?
        if (team.getSubscribedHackathon() == null || !team.getSubscribedHackathon().getId().equals(hackathon.getId())) {
            throw new RuleViolationException("The team is not subscribed to this Hackathon.");
        }

        // Controllo 2: La scadenza per le consegne è passata?
        // È importante verificare la data di scadenza esatta come da specifiche
        if (LocalDateTime.now().isAfter(hackathon.getSubmitDeadline())) {
            throw new RuleViolationException("The submission deadline has already passed.");
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
            Submission savedSubmission = unitOfWork.getSubmissionRepository().save(newSubmission);

            // 2. SINCRONIZZAZIONE DELLA RELAZIONE BIDIREZIONALE!
            // Aggiorniamo la lista in memoria del Team, altrimenti alla
            // prossima chiamata il team sembrerà non avere sottomissioni.
            team.getSubmissions().add(savedSubmission);
            hackathon.getSubmissions().add(savedSubmission);

            return mapToDTO(savedSubmission);
        } else {
            //TODO: oppure mappiamo la richiesta di creazione ad un update e ritorniamo l'update :)

            // Se esiste, informiamo che una sottomissione è già presente e che è possibile modificarla
            throw new RuleViolationException("A submission for this Hackathon already exists.");
        }
    }

    @Override
    public SubmissionResponseDTO updateSubmission(UpdateSubmissionDTO request, Long userId) {
        Submission submission = unitOfWork.getSubmissionRepository().findById(request.submissionId()).orElse(null);
        if (submission == null) {
            throw new ResourceNotFoundException("Submission not found.");
        }
        if (submission.getHackathon().getSubmitDeadline().isBefore(LocalDateTime.now())) {
            throw new RuleViolationException("The submission deadline has already passed.");
        }

        DefaultUser user = unitOfWork.getDefaultUserRepository().findById(userId).orElseThrow();
        if (user.getTeam() == null || !submission.getTeam().getId().equals(user.getTeam().getId())) {
            throw new UnauthorizedActionException("Non puoi modificare una sottomissione che non appartiene al tuo team.");
        }

        submission.setProjectUrl(request.projectUrl());
        submission.setDescription(request.description());
        submission.setSubmissionDate(LocalDateTime.now());
        unitOfWork.getSubmissionRepository().save(submission);

        return mapToDTO(submission);
    }

    @Override
    public SubmissionResponseDTO evaluateSubmission(EvaluateSubmissionDTO request, Long judgeId) {
        // 1. Recupero la sottomissione
        Submission submission = unitOfWork.getSubmissionRepository().findById(request.submissionId()).orElse(null);
        if (submission == null) {
            throw new ResourceNotFoundException("Sottomissione non trovata");
        }

        Hackathon hackathon = submission.getHackathon();

        // 2. Controllo di sicurezza: chi valuta è davvero il giudice di questo Hackathon?
        if (!hackathon.getJudge().getId().equals(judgeId)) {
            throw new UnauthorizedActionException("Non sei il giudice assegnato a questo Hackathon");
        }

        if (hackathon.getStatus() != HackathonStatus.UNDER_EVALUATION) {
            throw new RuleViolationException("L'hackathon non è attualmente in fase di valutazione");
        }

        //Solo closed perchè non puoi gestirla se aperta o valutata
        if (submission.getStatus() != SubmissionStatus.CLOSED ) {
            throw new RuleViolationException("Non puoi gestire questa sottomissione");
        }

        // 5. Aggiornamento dell'entità
        submission.setScore(request.score());
        submission.setJudgeFeedback(request.feedback());
        submission.setStatus(SubmissionStatus.EVALUATED);
        unitOfWork.getSubmissionRepository().save(submission);

        return mapToDTO(submission);
    }

    @Override
    public List<SubmissionResponseDTO> getSubmissionsByHackathon(Long hackathonId, Long staffId) {
        Hackathon hackathon = unitOfWork.getHackathonRepository().findById(hackathonId).orElse(null);
        if (hackathon == null) {
            throw new ResourceNotFoundException("Hackathon non trovato.");
        }

        // Controllo Sicurezza: lo staff fa parte di questo hackathon?
        if (!isStaffAssignedToHackathon(hackathon, staffId)) {
            throw new UnauthorizedActionException("Non sei autorizzato a visualizzare le sottomissioni di questo hackathon.");
        }

        List<Submission> submissions = unitOfWork.getSubmissionRepository().findByHackathonId(hackathonId);

        return submissions.stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public SubmissionResponseDTO getSubmissionDetails(Long submissionId, Long staffId) {
        Submission submission = unitOfWork.getSubmissionRepository().findById(submissionId).orElse(null);
        if (submission == null) {
            throw new ResourceNotFoundException("Sottomissione non trovata.");
        }

        // Controllo Sicurezza: lo staff fa parte dell'hackathon a cui appartiene questa sottomissione?
        if (!isStaffAssignedToHackathon(submission.getHackathon(), staffId)) {
            throw new UnauthorizedActionException("Non sei autorizzato a visualizzare i dettagli di questa sottomissione.");
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