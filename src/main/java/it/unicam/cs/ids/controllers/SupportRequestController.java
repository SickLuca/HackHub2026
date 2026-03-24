package it.unicam.cs.ids.controllers;

import it.unicam.cs.ids.dtos.requests.CreateSupportRequestDTO;
import it.unicam.cs.ids.dtos.responses.SupportRequestResponseDTO;
import it.unicam.cs.ids.services.abstractions.ISupportRequestService;

import java.util.List;

public class SupportRequestController {
    private final ISupportRequestService supportRequestService;

    public SupportRequestController(ISupportRequestService supportRequestService) {
        this.supportRequestService = supportRequestService;
    }

    public SupportRequestResponseDTO createSupportRequest(CreateSupportRequestDTO request) {
        return supportRequestService.createRequest(request);
    }

    public List<SupportRequestResponseDTO> getRequestsForHackathon(Long hackathonId, Long mentorId) {
        return supportRequestService.getRequestsForHackathon(hackathonId, mentorId);
    }

}