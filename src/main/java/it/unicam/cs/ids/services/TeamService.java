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

/**
 * Service for managing teams.
 * <p>
 * Contains the logic for creating a new team, registering it
 * for a hackathon, retrieving the current user's team information,
 * and handling a member leaving (including logic for disbanding
 * the team if the leader leaves).
 * </p>
 */
@Service
@Transactional
public class TeamService implements ITeamService {

    private final IUnitOfWork unitOfWork;

    public TeamService(IUnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    @Override
    public TeamResponseDTO createTeam(CreateTeamDTO request, Long creatorId) {
        // 1. Retrieve the user safely
        DefaultUser creator = unitOfWork.getDefaultUserRepository().findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found in the system"));

        // 2. Business rule check: a user can only belong to one team
        if (creator.getTeam() != null || creator.getRole() != UserRole.USER_NO_TEAM) {
            throw new RuleViolationException("The user already belongs to a team!");
        }

        // 3. Create the Team entity
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
        // 1. Retrieve the authenticated user and verify the role
        DefaultUser leader = unitOfWork.getDefaultUserRepository().findById(leaderId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (leader.getRole() != UserRole.TEAM_LEADER) {
            throw new UnauthorizedActionException("Only the leader can register the team for a hackathon");
        }

        // 2. Safely derive the team
        Team team = leader.getTeam();
        if (team == null) {
            throw new RuleViolationException("The team does not exist.");
        }

        // 3. Retrieve the Hackathon
        Hackathon hackathon = unitOfWork.getHackathonRepository().findById(request.hackathonId())
                .orElseThrow(() -> new ResourceNotFoundException("Hackathon not found"));

        if (hackathon.getStatus() != HackathonStatus.REGISTRATION) {
            throw new RuleViolationException("The Hackathon is not in the registration phase");
        }

        if (team.getSubscribedHackathon() != null) {
            throw new RuleViolationException("The team is already registered for a Hackathon.");
        }

        if (team.getMembers().size() > hackathon.getMaxDimensionOfTeam()) {
            throw new RuleViolationException("The team exceeds the maximum size for this hackathon");
        }

        // 4. Update the database
        team.setSubscribedHackathon(hackathon);
        hackathon.getTeams().add(team);

        unitOfWork.getTeamRepository().save(team);

        return mapToDTO(team);
    }
    
    @Override
    public TeamResponseDTO getTeamByCurrentUser(Long userId){
        DefaultUser user = unitOfWork.getDefaultUserRepository().findById(userId).orElse(null);
        if(user == null){
            throw new ResourceNotFoundException("User not found");
        }

        Team team = user.getTeam();

        if(team == null){
            throw new RuleViolationException("The user does not belong to any team");
        }

        return mapToDTO(team);
    }

    @Override
    public void leaveTeam(Long userId) {
        // 1. Retrieve the user
        DefaultUser user = unitOfWork.getDefaultUserRepository().findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Team team = user.getTeam();
        if (team == null) {
            throw new RuleViolationException("You do not belong to any team.");
        }

        // Save the role before modifying it
        boolean wasLeader = user.getRole() == UserRole.TEAM_LEADER;

        // 2. Disconnect the user from the team (update memory and entity)
        team.getMembers().remove(user);
        user.setTeam(null);
        user.setRole(UserRole.USER_NO_TEAM);

        // 3. Handle the team's fate
        if (team.getMembers().isEmpty()) {
            // The team is empty: disconnect the hackathon (if any) and delete the team
            if (team.getSubscribedHackathon() != null) {
                team.getSubscribedHackathon().getTeams().remove(team);
            }
            unitOfWork.getTeamRepository().delete(team);

        } else {
            // The team still has members: if the leader left, promote the first in the list
            if (wasLeader) {
                DefaultUser newLeader = team.getMembers().getFirst();
                newLeader.setRole(UserRole.TEAM_LEADER);
                unitOfWork.getDefaultUserRepository().save(newLeader);
            }
            unitOfWork.getTeamRepository().save(team);
        }

        // 4. Save the updated user
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