package it.unicam.cs.ids.utils.unitOfWork;

import it.unicam.cs.ids.repositories.abstractions.*;

public class UnitOfWork implements IUnitOfWork{

    private final IDefaultUserRepository defaultUserRepository;
    private final IHackathonRepository hackathonRepository;
    private final IStaffUserRepository staffUserRepository;
    private final ISubmissionRepository submissionRepository;
    private final ITeamRepository teamRepository;

    public UnitOfWork(IDefaultUserRepository defaultUserRepository, IHackathonRepository hackathonRepository, IStaffUserRepository staffUserRepository, ISubmissionRepository submissionRepository, ITeamRepository teamRepository) {
        this.defaultUserRepository = defaultUserRepository;
        this.hackathonRepository = hackathonRepository;
        this.staffUserRepository = staffUserRepository;
        this.submissionRepository = submissionRepository;
        this.teamRepository = teamRepository;
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
    public void commit() {

    }

    @Override
    public void rollback() {

    }
}
