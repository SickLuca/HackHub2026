package it.unicam.cs.ids.utils.scheduler;
import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.models.Submission;
import it.unicam.cs.ids.models.utils.HackathonStatus;
import it.unicam.cs.ids.models.utils.SubmissionStatus;
import it.unicam.cs.ids.utils.unitOfWork.IUnitOfWork;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler per le operazioni periodiche sugli Hackathon e sulle relative Submission.
 * <p>
 * Verifica a intervalli regolari le scadenze (es. registrationDeadline, submitDeadline)
 * e aggiorna automaticamente gli stati (es. da REGISTRATION_OPEN a IN_PROGRESS,
 * e da IN_PROGRESS a CLOSED) oltre allo stato delle relative submission.
 * </p>
 */
@Component
public class HackathonScheduler {

    private final IUnitOfWork unitOfWork;

    public HackathonScheduler(IUnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    /**
     * This task is being repeated every (60000 millisecondi).
     * Manages hackathon's state switches based on the date.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void updateHackathonStatuses() {
        LocalDateTime now = LocalDateTime.now();

        // --- 1. Da REGISTRATION a IN_PROGRESS ---
        // Recuperiamo tutti gli hackathon in fase di registrazione
        List<Hackathon> registrationHackathons = unitOfWork.getHackathonRepository().findAll().stream()
                .filter(h -> h.getStatus() == HackathonStatus.REGISTRATION)
                .toList();

        for (Hackathon h : registrationHackathons) {
            // Se la data di scadenza iscrizioni è passata
            if (now.isAfter(h.getRegistrationDeadline())) {
                h.setStatus(HackathonStatus.IN_PROGRESS);
                unitOfWork.getHackathonRepository().save(h);
                System.out.println("[Scheduler] Hackathon '" + h.getName() + "' is now IN_PROGRESS. Registration deadline passed.");
            }
        }

        // --- 2. Da IN_PROGRESS a UNDER_EVALUATION ---
        // Recuperiamo tutti gli hackathon in corso
        List<Hackathon> inProgressHackathons = unitOfWork.getHackathonRepository().findAll().stream()
                .filter(h -> h.getStatus() == HackathonStatus.IN_PROGRESS)
                .toList();

        for (Hackathon h : inProgressHackathons) {
            // Se la data limite per sottomissioni è passata
            if (now.isAfter(h.getSubmitDeadline())) {

                // 1. Cambiamo lo stato dell'Hackathon
                h.setStatus(HackathonStatus.UNDER_EVALUATION);
                unitOfWork.getHackathonRepository().save(h);

                // 2. Chiudiamo tutte le sottomissioni OPEN di questo hackathon
                List<Submission> submissions = unitOfWork.getSubmissionRepository().findByHackathonId(h.getId());
                for (Submission sub : submissions) {
                    if (sub.getStatus() == SubmissionStatus.OPEN) {
                        sub.setStatus(SubmissionStatus.CLOSED);
                        unitOfWork.getSubmissionRepository().save(sub);
                    }
                }

                System.out.println("[Scheduler] Hackathon '" + h.getName() + "' is now UNDER_EVALUATION. Submissions are now evaluable by the judge.");
            }
        }
    }
}