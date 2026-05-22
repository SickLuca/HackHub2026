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

/**
 * REST controller for managing support requests.
 * <p>
 * Allows team members to create help requests
 * and mentors to view and schedule support calls.
 * </p>
 */
@RestController
@RequestMapping("/api/supportRequest")
@Validated
public class SupportRequestController {
    private final ISupportRequestService supportRequestService;

    public SupportRequestController(ISupportRequestService supportRequestService) {
        this.supportRequestService = supportRequestService;
    }

    /**
     * Creates a new support request for the user's team.
     * <p>Accessible to users with the {@code TEAM_LEADER} or {@code TEAM_MEMBER} role.</p>
     *
     * @param request DTO containing the message and the referenced hackathon
     * @return {@link SupportRequestResponseDTO} with the details of the created request
     */
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('TEAM_LEADER', 'TEAM_MEMBER')")
    public ResponseEntity<SupportRequestResponseDTO> createSupportRequest(@Valid @RequestBody CreateSupportRequestDTO request) {
        Long userId = SecurityUtils.getAuthenticatedUserId();

        SupportRequestResponseDTO response = supportRequestService.createRequest(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Returns all support requests for a hackathon.
     * <p>Accessible only to users with the {@code MENTOR} role.
     * The mentor must be assigned to the specified hackathon.</p>
     *
     * @param hackathonId the ID of the hackathon whose requests to retrieve
     * @return list of {@link SupportRequestResponseDTO}
     */
    @GetMapping("/getAll")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<List<SupportRequestResponseDTO>> getRequestsForHackathon(@RequestParam
                                                                                       @NotNull(message = "The ID must be greater than 0")
                                                                                       @Positive(message = "The ID must be a positive number")
                                                                                       Long hackathonId) {
        Long mentorId = SecurityUtils.getAuthenticatedUserId();

        List<SupportRequestResponseDTO> response = supportRequestService.getRequestsForHackathon(hackathonId, mentorId);
        return ResponseEntity.ok(response);
    }

    /**
     * Schedules a support call for an existing request.
     * <p>Accessible only to users with the {@code MENTOR} role.
     * Generates a video call link via the integrated calendar service (Adapter pattern).</p>
     *
     * @param request DTO containing the request ID and call details
     * @return {@link SupportRequestResponseDTO} with the link to the scheduled call
     */
    @PostMapping("/scheduleCall")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<SupportRequestResponseDTO> scheduleCall(@Valid @RequestBody ScheduleCallDTO request) {
        Long mentorId = SecurityUtils.getAuthenticatedUserId();

        SupportRequestResponseDTO response = supportRequestService.scheduleCall(request, mentorId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}