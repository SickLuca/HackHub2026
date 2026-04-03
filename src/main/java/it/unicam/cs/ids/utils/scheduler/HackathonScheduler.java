package it.unicam.cs.ids.utils.scheduler;

import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.models.Submission;
import it.unicam.cs.ids.models.utils.HackathonStatus;
import it.unicam.cs.ids.models.utils.SubmissionStatus;
import it.unicam.cs.ids.repositories.IHackathonRepository;
import it.unicam.cs.ids.repositories.ISubmissionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class HackathonScheduler {

    private final IHackathonRepository hackathonRepository;
    private final ISubmissionRepository submissionRepository;

    public HackathonScheduler(IHackathonRepository hackathonRepository, ISubmissionRepository submissionRepository) {
        this.hackathonRepository = hackathonRepository;
        this.submissionRepository = submissionRepository;
    }

    /**
     * Questo task viene eseguito ogni minuto (60000 millisecondi).
     * Gestisce i passaggi di stato automatici dell'Hackathon in base alle date.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void updateHackathonStatuses() {
        LocalDateTime now = LocalDateTime.now();

        // --- 1. Da REGISTRATION a IN_PROGRESS ---
        // Recuperiamo tutti gli hackathon in fase di registrazione
        List<Hackathon> registrationHackathons = hackathonRepository.findAll().stream()
                .filter(h -> h.getStatus() == HackathonStatus.REGISTRATION)
                .toList();

        for (Hackathon h : registrationHackathons) {
            // Se la data di scadenza iscrizioni è passata
            if (now.isAfter(h.getRegistrationDeadline())) {
                h.setStatus(HackathonStatus.IN_PROGRESS);
                hackathonRepository.save(h);
                System.out.println("[Scheduler] Hackathon '" + h.getName() + "' è ora IN_PROGRESS. Le iscrizioni sono chiuse.");
            }
        }

        // --- 2. Da IN_PROGRESS a UNDER_EVALUATION ---
        // Recuperiamo tutti gli hackathon in corso
        List<Hackathon> inProgressHackathons = hackathonRepository.findAll().stream()
                .filter(h -> h.getStatus() == HackathonStatus.IN_PROGRESS)
                .toList();

        for (Hackathon h : inProgressHackathons) {
            // Se la data limite per sottomissioni è passata
            if (now.isAfter(h.getSubmitDeadline())) {

                // 1. Cambiamo lo stato dell'Hackathon
                h.setStatus(HackathonStatus.UNDER_EVALUATION);
                hackathonRepository.save(h);

                // 2. Chiudiamo tutte le sottomissioni OPEN di questo hackathon
                List<Submission> submissions = submissionRepository.findByHackathonId(h.getId());
                for (Submission sub : submissions) {
                    if (sub.getStatus() == SubmissionStatus.OPEN) {
                        sub.setStatus(SubmissionStatus.CLOSED);
                        submissionRepository.save(sub);
                    }
                }

                System.out.println("[Scheduler] Hackathon '" + h.getName() + "' è ora UNDER_EVALUATION. Sottomissioni chiuse per i giudici.");
            }
        }

    }
}