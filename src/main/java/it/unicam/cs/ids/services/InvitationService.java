package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.requests.CreateInvitationDTO;
import it.unicam.cs.ids.dtos.responses.InvitationResponseDTO;
import it.unicam.cs.ids.dtos.requests.RespondInvitationDTO;
import it.unicam.cs.ids.exceptions.InvalidInputException;
import it.unicam.cs.ids.exceptions.ResourceNotFoundException;
import it.unicam.cs.ids.exceptions.RuleViolationException;
import it.unicam.cs.ids.models.DefaultUser;
import it.unicam.cs.ids.models.Invitation;
import it.unicam.cs.ids.models.Team;
import it.unicam.cs.ids.models.utils.InvitationStatus;
import it.unicam.cs.ids.models.utils.UserRole;
import it.unicam.cs.ids.services.abstractions.IInvitationService;
import it.unicam.cs.ids.utils.unitOfWork.IUnitOfWork;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing team invitations.
 * <p>
 * Provides methods for team leaders to send invitations
 * to users without a team, and allows those users to view
 * their received invitations and accept or reject them,
 * updating team membership accordingly.
 * </p>
 */
@Service
@Transactional
public class InvitationService implements IInvitationService {

    private final IUnitOfWork unitOfWork;

    public InvitationService(IUnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    @Override
    public InvitationResponseDTO sendInvitation(CreateInvitationDTO request, Long fromTeamLeaderId) {

        DefaultUser inviter = unitOfWork.getDefaultUserRepository().findById(fromTeamLeaderId).orElse(null);
        // Check role validity and team membership
        if (inviter.getRole() != UserRole.TEAM_LEADER) {
            throw new RuleViolationException("Only a Team Leader can invite new members to the team.");
        }

        Team team = inviter.getTeam();
        // Check team capacity based on the hackathon (if registered)
        if (team.getSubscribedHackathon() != null) {
            if (team.getMembers().size() >= team.getSubscribedHackathon().getMaxDimensionOfTeam()) {
                throw new RuleViolationException("The team has already reached the maximum size for the hackathon it is registered in.");
            }
        }

        DefaultUser invitedUser = unitOfWork.getDefaultUserRepository().findById(request.toUserId()).orElse(null);
        if (invitedUser == null) {
            throw new ResourceNotFoundException("User with id " + request.toUserId() + " not found");
        }


        // Check the status of the invited user
        if (invitedUser.getRole() != UserRole.USER_NO_TEAM) {
            throw new RuleViolationException("The invited user already belongs to a team.");
        }


        // Create the entity
        Invitation invitation = new Invitation();
        invitation.setDescription(request.description());
        invitation.setFromTeam(team);
        invitation.setToUser(invitedUser);
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setCreationDate(LocalDateTime.now());

        Invitation savedInvitation = unitOfWork.getInvitationRepository().save(invitation);

        //Update user in memory
        invitedUser.getInvitations().add(savedInvitation);

        //Update user in the DB (should not be necessary as JPA should have updated the relations after setToUser)
        unitOfWork.getDefaultUserRepository().save(invitedUser);


        //update bidirectional relationship in memory
        team.getInvitations().add(savedInvitation);

        return mapToDTO(savedInvitation);
    }

    public List<InvitationResponseDTO> getAllInvitationsByCurrentUser(Long userId){

        if(userId == null){
            throw new InvalidInputException("User id is null");
        }

        List<InvitationResponseDTO> invitations = new ArrayList<>();

        unitOfWork.getInvitationRepository().findAll().stream()
                .filter(invitation -> invitation.getToUser().getId().equals(userId))
                .forEach(invitation -> invitations.add(mapToDTO(invitation)));

        return invitations;

    }

    @Override
    public InvitationResponseDTO respondToInvitation(RespondInvitationDTO request, Long userId){
        Invitation invitation = unitOfWork.getInvitationRepository().findById(request.invitationId()).orElse(null);
        if (invitation == null) {
            throw new ResourceNotFoundException("Invitation not found.");
        }

        // 2. Verify the invitation is still pending
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new RuleViolationException("This invitation has already been handled (accepted or rejected).");
        }

        // 3. Verify the user's identity
        DefaultUser user = unitOfWork.getDefaultUserRepository().findById(invitation.getToUser().getId()).orElse(null);
        if (user == null || !invitation.getToUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("The user is not the recipient of this invitation.");
        }

        if (request.accept()) {
            // 4. Check again that the user doesn't already have a team (e.g. they accepted another invitation 5 minutes ago)
            if (user.getRole() != UserRole.USER_NO_TEAM) {
                throw new RuleViolationException("Cannot accept: you already belong to a team.");
            }

            // 5. Check whether the team is currently registered for a hackathon
            Team team = invitation.getFromTeam();
            if (team.getSubscribedHackathon() != null) {
                throw new RuleViolationException("Cannot accept: the team is participating in a hackathon");
            }

            invitation.setStatus(InvitationStatus.ACCEPTED);

            // Update the user
            user.setTeam(team);
            user.setRole(UserRole.TEAM_MEMBER); // User becomes a team member

            // Sync the bidirectional relationship in memory (the database would be saved anyway through the team merge)
            team.getMembers().add(user);

            unitOfWork.getTeamRepository().save(team);
            unitOfWork.getDefaultUserRepository().save(user);
        } else {
            invitation.setStatus(InvitationStatus.REJECTED);
        }

        return mapToDTO(unitOfWork.getInvitationRepository().save(invitation));
    }


    private InvitationResponseDTO mapToDTO(Invitation invitation) {
        return new InvitationResponseDTO(
                invitation.getToUser().getName(),
                invitation.getFromTeam().getName(),
                invitation.getStatus(),
                invitation.getCreationDate()
        );
    }


}