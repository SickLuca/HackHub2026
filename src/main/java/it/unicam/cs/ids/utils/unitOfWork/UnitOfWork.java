package it.unicam.cs.ids.utils.unitOfWork;

import it.unicam.cs.ids.repositories.*;
import org.springframework.stereotype.Component;

/**
 * Concrete implementation of {@link IUnitOfWork}.
 * <p>
 * Centralizes the injection and access to the various Repositories (Spring Data JPA) in the system.
 * In Spring, actual transactionality is typically managed via
 * {@code @Transactional} in services, so this component acts
 * primarily as an organizational wrapper to reduce boilerplate.
 * </p>
 */
@Component
public class UnitOfWork implements IUnitOfWork{

    private final IDefaultUserRepository defaultUserRepository;
    private final IHackathonRepository hackathonRepository;
    private final IStaffUserRepository staffUserRepository;
    private final ISubmissionRepository submissionRepository;
    private final ITeamRepository teamRepository;
    private final IInvitationRepository invitationRepository;
    private final ISupportRequestRepository supportRequestRepository;
    private final IReportRepository reportRepository;
    private final IUserRepository userRepository;

    public UnitOfWork(IDefaultUserRepository defaultUserRepository,
                      IHackathonRepository hackathonRepository,
                      IStaffUserRepository staffUserRepository,
                      ISubmissionRepository submissionRepository,
                      ITeamRepository teamRepository,
                      IInvitationRepository invitationRepository,
                      ISupportRequestRepository supportRequestRepository,
                      IReportRepository reportRepository, IUserRepository userRepository) {
        this.defaultUserRepository = defaultUserRepository;
        this.hackathonRepository = hackathonRepository;
        this.staffUserRepository = staffUserRepository;
        this.submissionRepository = submissionRepository;
        this.teamRepository = teamRepository;
        this.invitationRepository = invitationRepository;
        this.supportRequestRepository = supportRequestRepository;
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
    }


    @Override
    public IDefaultUserRepository getDefaultUserRepository() {
        return this.defaultUserRepository;
    }

    @Override
    public IHackathonRepository getHackathonRepository() {
        return this.hackathonRepository;
    }

    @Override
    public IStaffUserRepository getStaffUserRepository() {
        return this.staffUserRepository;
    }

    @Override
    public ISubmissionRepository getSubmissionRepository() {
        return this.submissionRepository;
    }

    @Override
    public ITeamRepository getTeamRepository() {
        return this.teamRepository;
    }

    @Override
    public IInvitationRepository getInvitationRepository() {
        return this.invitationRepository;
    }

    @Override
    public ISupportRequestRepository getSupportRequestRepository() {return this.supportRequestRepository;}

    @Override
    public IReportRepository getReportRepository() {return this.reportRepository;}

    @Override
    public IUserRepository getUserRepository() {return this.userRepository;}

    @Override
    public void commit() {

    }

    @Override
    public void rollback() {

    }
}