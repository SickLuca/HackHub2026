package it.unicam.cs.ids.controllers;

import it.unicam.cs.ids.dtos.requests.CreateInvitationDTO;
import it.unicam.cs.ids.dtos.responses.InvitationResponseDTO;
import it.unicam.cs.ids.dtos.requests.RespondInvitationDTO;
import it.unicam.cs.ids.services.abstractions.IInvitationService;

import java.util.List;

public class InvitationController {

    private final IInvitationService invitationService;

    public InvitationController(IInvitationService invitationService) {
        this.invitationService = invitationService;
    }

    public InvitationResponseDTO sendInvitation(CreateInvitationDTO request) {
        return invitationService.sendInvitation(request);
    }

    public List<InvitationResponseDTO> getAllInvitationsByUserId(Long userId){
        return invitationService.getAllInvitationsByUserId(userId);
    }

    public InvitationResponseDTO respondToInvitation(RespondInvitationDTO response) {
        return invitationService.respondToInvitation(response);
    }

}
