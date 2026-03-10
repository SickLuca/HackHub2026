package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.CreateTeamDTO;
import it.unicam.cs.ids.models.DefaultUser;
import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.models.Team;
import it.unicam.cs.ids.models.utils.UserRole;
import it.unicam.cs.ids.repositories.abstractions.IDefaultUserRepository;
import it.unicam.cs.ids.repositories.abstractions.ITeamRepository;
import it.unicam.cs.ids.services.abstractions.ITeamService;
import it.unicam.cs.ids.validators.abstractions.Validator;

import java.util.ArrayList;

public class TeamService implements ITeamService {

    private final ITeamRepository teamRepository;
    private final IDefaultUserRepository defaultUserRepository;
    private final Validator<Team> teamValidator;

    public TeamService(ITeamRepository teamRepository, IDefaultUserRepository defaultUserRepository, Validator<Team> teamValidator) {
        this.teamRepository = teamRepository;
        this.defaultUserRepository = defaultUserRepository;
        this.teamValidator = teamValidator;
    }

    @Override
    public Team createTeam(CreateTeamDTO request) {
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

        if (teamValidator.validate(newTeam)) {
            newTeam = teamRepository.create(newTeam);

            // 5. Aggiornamento dell'utente creatore: gli assegno il team e gli cambio ruolo
            creator.setTeam(newTeam);
            creator.setRole(UserRole.TEAM_MEMBER);
            defaultUserRepository.update(creator);

            return newTeam;
        } else {
            throw new IllegalArgumentException("Hackathon creation failed: invalid data");
        }
    }
}