package it.unicam.cs.ids.services.abstractions;

import it.unicam.cs.ids.dtos.requests.CreateInvitationDTO;
import it.unicam.cs.ids.dtos.responses.InvitationResponseDTO;
import it.unicam.cs.ids.dtos.requests.RespondInvitationDTO;

import java.util.List;

public interface IInvitationService {
    InvitationResponseDTO sendInvitation(CreateInvitationDTO request);
    List<InvitationResponseDTO> getAllInvitationsByUserId(Long userId);
    InvitationResponseDTO respondToInvitation(RespondInvitationDTO response);

}
