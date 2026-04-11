package it.unicam.cs.ids.controllers;

import it.unicam.cs.ids.dtos.requests.CreateSupportRequestDTO;
import it.unicam.cs.ids.dtos.requests.ScheduleCallDTO;
import it.unicam.cs.ids.dtos.responses.SupportRequestResponseDTO;
import it.unicam.cs.ids.security.SecurityUtils;
import it.unicam.cs.ids.services.abstractions.ISupportRequestService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supportRequest")
@Validated
public class SupportRequestController {
    private final ISupportRequestService supportRequestService;

    public SupportRequestController(ISupportRequestService supportRequestService) {
        this.supportRequestService = supportRequestService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('TEAM_LEADER', 'TEAM_MEMBER')")
    public ResponseEntity<SupportRequestResponseDTO> createSupportRequest(@Valid @RequestBody CreateSupportRequestDTO request) {
        Long userId = SecurityUtils.getAuthenticatedUserId();

        SupportRequestResponseDTO response = supportRequestService.createRequest(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/getAll")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<List<SupportRequestResponseDTO>> getRequestsForHackathon(@RequestParam
                                                                                       @NotNull(message = "L'id deve essere maggiore di 0")
                                                                                       @Positive(message = "L'id deve essere un numero positivo")
                                                                                       Long hackathonId) {
        Long mentorId = SecurityUtils.getAuthenticatedUserId();

        List<SupportRequestResponseDTO> response = supportRequestService.getRequestsForHackathon(hackathonId, mentorId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/scheduleCall")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<SupportRequestResponseDTO> scheduleCall(@Valid @RequestBody ScheduleCallDTO request) {
        Long mentorId = SecurityUtils.getAuthenticatedUserId();

        SupportRequestResponseDTO response = supportRequestService.scheduleCall(request, mentorId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}