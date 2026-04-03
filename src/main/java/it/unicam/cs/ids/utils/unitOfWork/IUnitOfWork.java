package it.unicam.cs.ids.utils.unitOfWork;

import it.unicam.cs.ids.repositories.*;


/** 
Unit of Work Wrapper" per disaccoppiare i Service dall'iniezione multipla di innumerevoli Repository.
Viene lasciata quindi la gestione transazionale a Spring e i metodi commit e rollback sono vuoti.*/

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