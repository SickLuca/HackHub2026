package it.unicam.cs.ids.utils.unitOfWork;

import it.unicam.cs.ids.repositories.abstractions.*;

public interface IUnitOfWork {

    IDefaultUserRepository getDefaultUserRepository();
    IHackathonRepository getHackathonRepository();
    IStaffUserRepository getStaffUserRepository();
    ISubmissionRepository getSubmissionRepository();
    ITeamRepository getTeamRepository();
    IInvitationRepository getInvitationRepository();
    ISupportRequestRepository getSupportRequestRepository();
    void commit();
    void rollback();
}
