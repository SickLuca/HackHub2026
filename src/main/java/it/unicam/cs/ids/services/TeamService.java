package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.requests.CreateTeamDTO;
import it.unicam.cs.ids.dtos.requests.SubscribeTeamDTO;
import it.unicam.cs.ids.dtos.responses.TeamResponseDTO;
import it.unicam.cs.ids.exceptions.ResourceNotFoundException;
import it.unicam.cs.ids.exceptions.RuleViolationException;
import it.unicam.cs.ids.exceptions.UnauthorizedActionException;
import it.unicam.cs.ids.models.DefaultUser;
import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.models.Report;
import it.unicam.cs.ids.models.Team;
import it.unicam.cs.ids.models.utils.HackathonStatus;
import it.unicam.cs.ids.models.utils.UserRole;
import it.unicam.cs.ids.services.abstractions.ITeamService;
import it.unicam.cs.ids.utils.unitOfWork.IUnitOfWork;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@Transactional
public class TeamService implements ITeamService {

    private final IUnitOfWork unitOfWork;

    public TeamService(IUnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    @Override
    public TeamResponseDTO createTeam(CreateTeamDTO request, Long creatorId) {
        // 1. Recupero l'utente in modo sicuro
        DefaultUser creator = unitOfWork.getDefaultUserRepository().findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato nel sistema"));

        // 2. Controllo regole di business: un utente può appartenere a un solo team
        if (creator.getTeam() != null || creator.getRole() != UserRole.USER_NO_TEAM) {
            throw new RuleViolationException("L'utente appartiene già a un team!");
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
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));

        if (leader.getRole() != UserRole.TEAM_LEADER) {
            throw new UnauthorizedActionException("Solo il leader può iscrivere il team a un hackathon");
        }

        // 2. Deduzione sicura del team
        Team team = leader.getTeam();
        if (team == null) {
            throw new RuleViolationException("Il team non esiste.");
        }

        // 3. Recupero Hackathon
        Hackathon hackathon = unitOfWork.getHackathonRepository().findById(request.hackathonId())
                .orElseThrow(() -> new ResourceNotFoundException("Hackathon non trovato"));

        if (hackathon.getStatus() != HackathonStatus.REGISTRATION) {
            throw new RuleViolationException("L'Hackathon non è in fase di registrazione");
        }

        if (team.getSubscribedHackathon() != null) {
            throw new RuleViolationException("Il team è già iscritto a un Hackathon.");
        }

        if (team.getMembers().size() > hackathon.getMaxDimensionOfTeam()) {
            throw new RuleViolationException("Il team supera la dimensione massima per questo hackathon");
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
            throw new ResourceNotFoundException("Utente non trovato");
        }

        Team team = user.getTeam();

        if(team == null){
            throw new RuleViolationException("L'utente non appartiene a nessun team");
        }

        return mapToDTO(team);
    }

    @Override
    public void leaveTeam(Long userId) {
        // 1. Recupero l'utente
        DefaultUser user = unitOfWork.getDefaultUserRepository().findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));

        Team team = user.getTeam();
        if (team == null) {
            throw new RuleViolationException("Non appartieni a nessun team.");
        }

        // Salviamo il ruolo prima di modificarlo
        boolean wasLeader = user.getRole() == UserRole.TEAM_LEADER;

        // 2. Scollego l'utente dal team (aggiorno memoria ed entità)
        team.getMembers().remove(user);
        user.setTeam(null);
        user.setRole(UserRole.USER_NO_TEAM);

        // 3. Gestisco il destino del team
        if (team.getMembers().isEmpty()) {
            // Il team è vuoto: scolleghiamo l'hackathon (se presente) e lo eliminiamo
            if (team.getSubscribedHackathon() != null) {
                team.getSubscribedHackathon().getTeams().remove(team);
            }
            unitOfWork.getTeamRepository().delete(team);

        } else {
            // Il team ha ancora membri: se è uscito il leader, promuovo il primo della lista
            if (wasLeader) {
                DefaultUser newLeader = team.getMembers().getFirst();
                newLeader.setRole(UserRole.TEAM_LEADER);
                unitOfWork.getDefaultUserRepository().save(newLeader);
            }
            unitOfWork.getTeamRepository().save(team);
        }

        // 4. Salvo l'utente aggiornato
        unitOfWork.getDefaultUserRepository().save(user);
    }
    
    private TeamResponseDTO mapToDTO(Team team) {
        return new TeamResponseDTO(
                team.getId(),
                team.getName(),
                team.getMembers().stream().map(m -> m.getName() + " " + m.getSurname()).toList(),
                team.getSubscribedHackathon() == null ? "Not subscribed to any Hackathon" : team.getSubscribedHackathon().getName(),
                team.getBalance(),
                //team.getInvitations().stream().map(i -> i.getDescription()).toList()
                team.getReports().stream().map(Report::getDescription).toList()
        );
    }

}