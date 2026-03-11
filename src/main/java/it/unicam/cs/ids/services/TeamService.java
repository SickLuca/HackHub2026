package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.CreateSubmissionDTO;
import it.unicam.cs.ids.dtos.CreateTeamDTO;
import it.unicam.cs.ids.dtos.SubscribeTeamDTO;
import it.unicam.cs.ids.models.DefaultUser;
import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.models.Submission;
import it.unicam.cs.ids.models.Team;
import it.unicam.cs.ids.models.utils.HackathonStatus;
import it.unicam.cs.ids.models.utils.UserRole;
import it.unicam.cs.ids.repositories.HackathonRepository;
import it.unicam.cs.ids.repositories.abstractions.IDefaultUserRepository;
import it.unicam.cs.ids.repositories.abstractions.IHackathonRepository;
import it.unicam.cs.ids.repositories.abstractions.ISubmissionRepository;
import it.unicam.cs.ids.repositories.abstractions.ITeamRepository;
import it.unicam.cs.ids.services.abstractions.ITeamService;
import it.unicam.cs.ids.validators.abstractions.Validator;

import java.util.ArrayList;
import java.util.Optional;

public class TeamService implements ITeamService {

    private final ITeamRepository teamRepository;
    private final IDefaultUserRepository defaultUserRepository;
    private final Validator<CreateTeamDTO> teamValidator;
    private final IHackathonRepository hackathonRepository;
    private final ISubmissionRepository submissionRepository;

    public TeamService(ITeamRepository teamRepository, IDefaultUserRepository defaultUserRepository, Validator<CreateTeamDTO> teamValidator, IHackathonRepository hackathonRepository, ISubmissionRepository submissionRepository) {
        this.teamRepository = teamRepository;
        this.defaultUserRepository = defaultUserRepository;
        this.teamValidator = teamValidator;
        this.hackathonRepository = hackathonRepository;
        this.submissionRepository = submissionRepository;
    }

    @Override
    public Team createTeam(CreateTeamDTO request) {
        teamValidator.validate(request); // Se fallisce, lancia l'eccezione ed esce dal metodo

        // 1. Recupero l'utente dal Database
        DefaultUser creator = defaultUserRepository.getById(request.creatorId());
        if (creator == null) {
            throw new IllegalArgumentException("User not found in the system");
        }

        // 2. Controllo le regole di business: un utente può appartenere a un solo team
        if (creator.getTeam() != null) {
            throw new IllegalStateException("User already belongs to a team!");
        }

        // 3. Creazione dell'entità Team
        Team newTeam = new Team();
        newTeam.setName(request.name());
        newTeam.setMembers(new ArrayList<>());
        newTeam.getMembers().add(creator);

        newTeam = teamRepository.create(newTeam);

        creator.setTeam(newTeam);
        creator.setRole(UserRole.TEAM_MEMBER);
        defaultUserRepository.update(creator);

        return newTeam;
    }

    @Override
    public Team subscribeHackathon(SubscribeTeamDTO request) {
        Team team = teamRepository.getById(request.teamId());
        if (team == null) {
            throw new IllegalArgumentException("Team not found in the system");
        }

        Hackathon hackathon = hackathonRepository.getById(request.hackathonId());
        if (hackathon == null) {
            throw new IllegalArgumentException("Hackathon not found in the system");
        }

        if (hackathon.getStatus() != HackathonStatus.REGISTRATION) {
            throw new IllegalStateException("Hackathon is not in registration phase");
        }

        if (team.getSubscribedHackathon() != null) {
            throw new IllegalStateException("Team is already subscribed to an Hackathon.");
        }

        if (team.getMembers().size() > hackathon.getMaxDimensionOfTeam()) {
            throw new IllegalStateException("Team is too big for the hackathon");
        }

        //Finite le validazioni, aggiorniamo sul database
        team.setSubscribedHackathon(hackathon);
        return teamRepository.update(team);

    }

}