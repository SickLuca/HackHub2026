package it.unicam.cs.ids.utils.unitOfWork;

import it.unicam.cs.ids.repositories.*;


/**
 * "Unit of Work Wrapper" to decouple Services from injecting multiple Repositories.
 * Transactional management is delegated to Spring, so the commit and rollback methods are empty.
 */

public interface IUnitOfWork {

    IDefaultUserRepository getDefaultUserRepository();
    IHackathonRepository getHackathonRepository();
    IStaffUserRepository getStaffUserRepository();
    ISubmissionRepository getSubmissionRepository();
    ITeamRepository getTeamRepository();
    IInvitationRepository getInvitationRepository();
    ISupportRequestRepository getSupportRequestRepository();
    IReportRepository getReportRepository();
    IUserRepository getUserRepository();
    void commit();
    void rollback();
}