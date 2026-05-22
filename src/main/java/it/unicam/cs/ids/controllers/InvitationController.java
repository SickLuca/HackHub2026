package it.unicam.cs.ids.controllers;

import it.unicam.cs.ids.dtos.requests.CreateInvitationDTO;
import it.unicam.cs.ids.dtos.responses.InvitationResponseDTO;
import it.unicam.cs.ids.dtos.requests.RespondInvitationDTO;
import it.unicam.cs.ids.security.SecurityUtils;
import it.unicam.cs.ids.services.abstractions.IInvitationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing team invitations.
 * <p>
 * Allows team leaders to send invitations to other users,
 * allows users to view their received invitations,
 * and to accept or reject them.
 * </p>
 */
@RestController
@RequestMapping("/api/invitations")
public class InvitationController {

    private final IInvitationService invitationService;

    public InvitationController(IInvitationService invitationService) {
        this.invitationService = invitationService;
    }

    /**
     * Sends an invitation to a user to join the sender's team.
     * <p>Accessible only to users with the {@code TEAM_LEADER} role.</p>
     *
     * @param request DTO containing the ID of the user to invite and the relevant hackathon ID
     * @return {@link InvitationResponseDTO} with the details of the created invitation
     */
    @PostMapping("/send")
    @PreAuthorize("hasRole('TEAM_LEADER')")
    public ResponseEntity<InvitationResponseDTO> sendInvitation(@Valid @RequestBody CreateInvitationDTO request) {
        Long fromTeamLeaderId = SecurityUtils.getAuthenticatedUserId();

        InvitationResponseDTO response = invitationService.sendInvitation(request,fromTeamLeaderId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Returns all invitations received by the authenticated user.
     * <p>Accessible to users with the {@code USER_NO_TEAM}, {@code TEAM_MEMBER}, or {@code TEAM_LEADER} role.</p>
     *
     * @return list of {@link InvitationResponseDTO} with all invitations for the current user
     */
    @GetMapping("/getAll")
    @PreAuthorize("hasAnyRole('USER_NO_TEAM', 'TEAM_MEMBER', 'TEAM_LEADER')")
    public ResponseEntity<List<InvitationResponseDTO>> getMyInvitations(){
        Long userId = SecurityUtils.getAuthenticatedUserId();

        List<InvitationResponseDTO> response = invitationService.getAllInvitationsByCurrentUser(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Responds to a received invitation, accepting or rejecting it.
     * <p>Accessible only to users with the {@code USER_NO_TEAM} role.
     * If accepted, the user will be added to the team that sent the invitation.</p>
     *
     * @param response DTO containing the invitation ID and the response (accepted/rejected)
     * @return {@link InvitationResponseDTO} with the updated invitation status
     */
    @PostMapping("/respond")
    @PreAuthorize("hasRole('USER_NO_TEAM')")
    public ResponseEntity<InvitationResponseDTO> respondToInvitation(@Valid @RequestBody RespondInvitationDTO response) {
        Long userId = SecurityUtils.getAuthenticatedUserId();

        InvitationResponseDTO invitationResponse = invitationService.respondToInvitation(response, userId);
        return new ResponseEntity<>(invitationResponse, HttpStatus.OK);
    }

}