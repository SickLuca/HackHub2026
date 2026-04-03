package it.unicam.cs.ids.utils.unitOfWork;

import it.unicam.cs.ids.repositories.*;

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