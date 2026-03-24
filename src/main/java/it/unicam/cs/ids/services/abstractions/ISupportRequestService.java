package it.unicam.cs.ids.services.abstractions;

import it.unicam.cs.ids.dtos.requests.CreateSupportRequestDTO;
import it.unicam.cs.ids.dtos.responses.SupportRequestResponseDTO;

import java.util.List;

public interface ISupportRequestService {

    SupportRequestResponseDTO createRequest(CreateSupportRequestDTO requestDTO);

    List<SupportRequestResponseDTO> getRequestsForHackathon(Long hackathonId, Long mentorId);
}