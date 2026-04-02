package it.unicam.cs.ids.controllers;

import it.unicam.cs.ids.dtos.requests.CreateSupportRequestDTO;
import it.unicam.cs.ids.dtos.responses.SupportRequestResponseDTO;
import it.unicam.cs.ids.security.SecurityUtils;
import it.unicam.cs.ids.services.abstractions.ISupportRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supportRequest")
public class SupportRequestController {
    private final ISupportRequestService supportRequestService;

    public SupportRequestController(ISupportRequestService supportRequestService) {
        this.supportRequestService = supportRequestService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('TEAM_LEADER', 'TEAM_MEMBER')")
    public ResponseEntity<SupportRequestResponseDTO> createSupportRequest(@RequestBody CreateSupportRequestDTO request) {
        Long userId = SecurityUtils.getAuthenticatedUserId();

        SupportRequestResponseDTO response = supportRequestService.createRequest(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/getAll")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<List<SupportRequestResponseDTO>> getRequestsForHackathon(@RequestParam Long hackathonId) {
        Long mentorId = SecurityUtils.getAuthenticatedUserId();

        List<SupportRequestResponseDTO> response = supportRequestService.getRequestsForHackathon(hackathonId, mentorId);
        return ResponseEntity.ok(response);
    }

}