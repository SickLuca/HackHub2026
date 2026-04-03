package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.requests.CreateTeamDTO;
import it.unicam.cs.ids.dtos.requests.SubscribeTeamDTO;
import it.unicam.cs.ids.dtos.responses.TeamResponseDTO;
import it.unicam.cs.ids.models.DefaultUser;
import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.models.Team;
import it.unicam.cs.ids.models.utils.HackathonStatus;
import it.unicam.cs.ids.models.utils.UserRole;
import it.unicam.cs.ids.services.abstractions.ITeamService;
import it.unicam.cs.ids.utils.unitOfWork.IUnitOfWork;
import it.unicam.cs.ids.validators.abstractions.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@Transactional
public class TeamService implements ITeamService {

    private final IUnitOfWork unitOfWork;
    private final Validator<CreateTeamDTO> teamValidator;

    public TeamService(IUnitOfWork unitOfWork,
                       Validator<CreateTeamDTO> teamValidator) {
        this.unitOfWork = unitOfWork;
        this.teamValidator = teamValidator;
    }

    @Override
    public TeamResponseDTO createTeam(CreateTeamDTO request, Long creatorId) {
        teamValidator.validate(request); // Se fallisce, lancia l'eccezione ed esce dal metodo

        // 1. Recupero l'utente in modo sicuro
        DefaultUser creator = unitOfWork.getDefaultUserRepository().findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato nel sistema"));

        // 2. Controllo regole di business: un utente può appartenere a un solo team
        if (creator.getTeam() != null || creator.getRole() != UserRole.USER_NO_TEAM) {
            throw new IllegalStateException("L'utente appartiene già a un team!");
        }

        // 3. Creazione dell'entità Team
        Team newTeam = new Team();
        newTeam.setName(request.name());
        newTeam.setMembers(new ArrayList<>());
        newTeam.getMembers().add(creator);

        unitOfWork.getTeamRepository().save(newTeam);

        creator.setTeam(newTeam);
        creator.setRole(UserRole.TEAM_LEADER);
        unitOfWork.getDefaultUserRepository().save(creator);

        return mapToDTO(newTeam);
    }

    @Override
    public TeamResponseDTO subscribeToHackathon(SubscribeTeamDTO request, Long leaderId) {
        // 1. Recupero l'utente autenticato e verifico il ruolo
        DefaultUser leader = unitOfWork.getDefaultUserRepository().findById(leaderId)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));

        if (leader.getRole() != UserRole.TEAM_LEADER) {
            throw new SecurityException("Solo il leader può iscrivere il team a un hackathon");
        }

        // 2. Deduzione sicura del team
        Team team = leader.getTeam();
        if (team == null) {
            throw new IllegalStateException("Il team non esiste.");
        }

        // 3. Recupero Hackathon
        Hackathon hackathon = unitOfWork.getHackathonRepository().findById(request.hackathonId())
                .orElseThrow(() -> new IllegalArgumentException("Hackathon non trovato"));

        if (hackathon.getStatus() != HackathonStatus.REGISTRATION) {
            throw new IllegalStateException("L'Hackathon non è in fase di registrazione");
        }

        if (team.getSubscribedHackathon() != null) {
            throw new IllegalStateException("Il team è già iscritto a un Hackathon.");
        }

        if (team.getMembers().size() > hackathon.getMaxDimensionOfTeam()) {
            throw new IllegalStateException("Il team supera la dimensione massima per questo hackathon");
        }

        // 4. Aggiorniamo sul database
        team.setSubscribedHackathon(hackathon);
        hackathon.getTeams().add(team);

        unitOfWork.getTeamRepository().save(team);

        return mapToDTO(team);
    }
    
    @Override
    public TeamResponseDTO getTeamByCurrentUser(Long userId){
        DefaultUser user = unitOfWork.getDefaultUserRepository().findById(userId).orElse(null);
        if(user == null){
            throw new IllegalArgumentException("Utente non trovato");
        }

        Team team = user.getTeam();

        if(team == null){
            throw new IllegalStateException("L'utente non appartiene a nessun team");
        }

        return mapToDTO(team);
    }
    
    private TeamResponseDTO mapToDTO(Team team) {
        return new TeamResponseDTO(
                team.getId(),
                team.getName(),
                team.getMembers().stream().map(m -> m.getName() + " " + m.getSurname()).toList(),
                team.getSubscribedHackathon() == null ? "Not subscribed to any Hackathon" : team.getSubscribedHackathon().getName(),
                team.getBalance()
        );
    }

}