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
 * Scheduler for periodic operations on Hackathons and their related Submissions.
 * <p>
 * Periodically checks deadlines (e.g. registrationDeadline, submitDeadline)
 * and automatically updates statuses (e.g. from REGISTRATION_OPEN to IN_PROGRESS,
 * and from IN_PROGRESS to CLOSED) as well as the status of related submissions.
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

        // --- 1. From REGISTRATION to IN_PROGRESS ---
        // Retrieve all hackathons in the registration phase
        List<Hackathon> registrationHackathons = unitOfWork.getHackathonRepository().findAll().stream()
                .filter(h -> h.getStatus() == HackathonStatus.REGISTRATION)
                .toList();

        for (Hackathon h : registrationHackathons) {
            // If the registration deadline has passed
            if (now.isAfter(h.getRegistrationDeadline())) {
                h.setStatus(HackathonStatus.IN_PROGRESS);
                unitOfWork.getHackathonRepository().save(h);
                System.out.println("[Scheduler] Hackathon '" + h.getName() + "' is now IN_PROGRESS. Registration deadline passed.");
            }
        }

        // --- 2. From IN_PROGRESS to UNDER_EVALUATION ---
        // Retrieve all hackathons currently in progress
        List<Hackathon> inProgressHackathons = unitOfWork.getHackathonRepository().findAll().stream()
                .filter(h -> h.getStatus() == HackathonStatus.IN_PROGRESS)
                .toList();

        for (Hackathon h : inProgressHackathons) {
            // If the submission deadline has passed
            if (now.isAfter(h.getSubmitDeadline())) {

                // 1. Change the Hackathon status
                h.setStatus(HackathonStatus.UNDER_EVALUATION);
                unitOfWork.getHackathonRepository().save(h);

                // 2. Close all OPEN submissions for this hackathon
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