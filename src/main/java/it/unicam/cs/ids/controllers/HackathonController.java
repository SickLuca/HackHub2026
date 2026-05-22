package it.unicam.cs.ids.controllers;

import it.unicam.cs.ids.dtos.requests.AddMentorDTO;
import it.unicam.cs.ids.dtos.requests.CreateHackathonDTO;
import it.unicam.cs.ids.dtos.requests.ProclaimWinnerDTO;
import it.unicam.cs.ids.dtos.responses.HackathonPublicResponseDTO;
import it.unicam.cs.ids.dtos.responses.HackathonResponseDTO;
import it.unicam.cs.ids.security.SecurityUtils;
import it.unicam.cs.ids.services.abstractions.IHackathonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for hackathon management.
 * <p>
 * Provides endpoints for creating, querying,
 * assigning mentors, and proclaiming the winner of a hackathon.
 * Write operations are reserved to users with the {@code ORGANIZER} role.
 * </p>
 */
@RestController
@RequestMapping("/api/hackathon")
public class HackathonController {

    private final IHackathonService hackathonService;

    public HackathonController(IHackathonService hackathonService) {
        this.hackathonService = hackathonService;
    }

    /**
     * Creates a new hackathon.
     * <p>Accessible only to users with the {@code ORGANIZER} role.</p>
     *
     * @param request DTO with the hackathon data (title, description, dates, etc.)
     * @return {@link HackathonResponseDTO} with the details of the newly created hackathon
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<HackathonResponseDTO> createHackathon(@Valid @RequestBody CreateHackathonDTO request) {
        Long organizerId = SecurityUtils.getAuthenticatedUserId();
        HackathonResponseDTO response = hackathonService.addHackathon(request,organizerId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Returns the complete list of all hackathons in the system.
     * <p>Accessible to any authenticated user.</p>
     *
     * @return list of {@link HackathonResponseDTO} with full details of each hackathon
     */
    @GetMapping("/getAll")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<HackathonResponseDTO>> getAllHackathons() {
        List<HackathonResponseDTO> response = hackathonService.getAllHackathons();
        return ResponseEntity.ok(response);
    }

    /**
     * Adds a mentor to an existing hackathon.
     * <p>Accessible only to users with the {@code ORGANIZER} role.
     * The organizer must be the owner of the hackathon.</p>
     *
     * @param request DTO containing the hackathon ID and the mentor ID to add
     * @return updated {@link HackathonResponseDTO} with the new mentor
     */
    @PostMapping("/addMentors")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<HackathonResponseDTO> addMentorToHackathon(@Valid @RequestBody AddMentorDTO request) {
        Long organizerId = SecurityUtils.getAuthenticatedUserId();
        HackathonResponseDTO response = hackathonService.addMentorToHackathon(request, organizerId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Proclaims the winner of a hackathon.
     * <p>Accessible only to users with the {@code ORGANIZER} role.
     * The hackathon must be in the appropriate state for proclamation.</p>
     *
     * @param request DTO containing the hackathon ID and the winning team ID
     * @return updated {@link HackathonResponseDTO} with the proclaimed winner
     */
    @PostMapping("/proclaimWinner")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<HackathonResponseDTO> proclaimWinner(@Valid @RequestBody ProclaimWinnerDTO request) {
        Long organizerId = SecurityUtils.getAuthenticatedUserId();

        HackathonResponseDTO response = hackathonService.proclaimWinner(request, organizerId);

        return ResponseEntity.ok(response);
    }

    /**
     * Returns public information for all hackathons.
     * <p>Exposes a reduced subset of data, suitable for display
     * in contexts where full details are not required (e.g. homepage).</p>
     *
     * @return list of {@link HackathonPublicResponseDTO} with public information
     */
    @GetMapping("/getPublicInfo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<HackathonPublicResponseDTO>> getPublicHackathons() {
        return ResponseEntity.ok(hackathonService.getHackathonsPublicInfo());
    }

}
