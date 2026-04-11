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

@RestController // 1. Dice a Spring che questa classe gestisce richieste HTTP e restituisce JSON
@RequestMapping("/api/invitations")
public class InvitationController {

    private final IInvitationService invitationService;

    public InvitationController(IInvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping("/send")
    @PreAuthorize("hasRole('TEAM_LEADER')")
    public ResponseEntity<InvitationResponseDTO> sendInvitation(@Valid @RequestBody CreateInvitationDTO request) {
        Long fromTeamLeaderId = SecurityUtils.getAuthenticatedUserId();

        InvitationResponseDTO response = invitationService.sendInvitation(request,fromTeamLeaderId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/getAll")
    @PreAuthorize("hasAnyRole('USER_NO_TEAM', 'TEAM_MEMBER', 'TEAM_LEADER')")
    public ResponseEntity<List<InvitationResponseDTO>> getMyInvitations(){
        Long userId = SecurityUtils.getAuthenticatedUserId();

        List<InvitationResponseDTO> response = invitationService.getAllInvitationsByCurrentUser(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/respond")
    @PreAuthorize("hasRole('USER_NO_TEAM')")
    public ResponseEntity<InvitationResponseDTO> respondToInvitation(@Valid @RequestBody RespondInvitationDTO response) {
        Long userId = SecurityUtils.getAuthenticatedUserId();

        InvitationResponseDTO invitationResponse = invitationService.respondToInvitation(response, userId);
        return new ResponseEntity<>(invitationResponse, HttpStatus.OK);
    }

}