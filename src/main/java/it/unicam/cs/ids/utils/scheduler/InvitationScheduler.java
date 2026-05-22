package it.unicam.cs.ids.utils.scheduler;

import it.unicam.cs.ids.models.Invitation;
import it.unicam.cs.ids.models.utils.InvitationStatus;
import it.unicam.cs.ids.utils.unitOfWork.IUnitOfWork;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler for periodic operations on Invitations.
 * <p>
 * Runs background tasks to check which invitations have exceeded
 * the maximum allowed time and updates their status to EXPIRED.
 * </p>
 */
@Component
public class InvitationScheduler {

    private final IUnitOfWork unitOfWork;

    public InvitationScheduler(IUnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void updateInvitationStatuses() {
        LocalDateTime now = LocalDateTime.now();
        List<Invitation> invitations = unitOfWork.getInvitationRepository() .findAll().stream()
                .filter(i -> i.getStatus() == InvitationStatus.PENDING)
                .toList();

        for (Invitation i : invitations) {
            if (now.isAfter(i.getCreationDate().plusDays(7))) {
                i.setStatus(InvitationStatus.EXPIRED);
                unitOfWork.getInvitationRepository().save(i);
                System.out.println("[Scheduler] Invitation '" + i.getId() + " is now expired.");
            }
        }
    }
}