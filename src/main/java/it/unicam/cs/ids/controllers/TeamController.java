package it.unicam.cs.ids.controllers;

import it.unicam.cs.ids.dtos.requests.CreateTeamDTO;
import it.unicam.cs.ids.dtos.requests.SubscribeTeamDTO;
import it.unicam.cs.ids.dtos.responses.TeamResponseDTO;
import it.unicam.cs.ids.security.SecurityUtils;
import it.unicam.cs.ids.services.abstractions.ITeamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final ITeamService teamService;

    public TeamController(ITeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('USER_NO_TEAM')")
    public ResponseEntity<TeamResponseDTO> createTeam(@Valid @RequestBody CreateTeamDTO request) {
        Long userId = SecurityUtils.getAuthenticatedUserId();

        TeamResponseDTO response = teamService.createTeam(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/subscribe")
    @PreAuthorize("hasRole('TEAM_LEADER')")
    public ResponseEntity<TeamResponseDTO> subscribeToHackathon(@Valid @RequestBody SubscribeTeamDTO request) {
        Long leaderId = SecurityUtils.getAuthenticatedUserId();

        TeamResponseDTO response = teamService.subscribeToHackathon(request, leaderId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER_NO_TEAM', 'TEAM_MEMBER', 'TEAM_LEADER')")
    public ResponseEntity<TeamResponseDTO> getMyTeam() {
        Long userId = SecurityUtils.getAuthenticatedUserId();

        TeamResponseDTO response = teamService.getTeamByCurrentUser(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/leave")
    @PreAuthorize("hasAnyRole('TEAM_MEMBER', 'TEAM_LEADER')")
    public ResponseEntity<String> leaveTeam() {
        Long userId = SecurityUtils.getAuthenticatedUserId();

        teamService.leaveTeam(userId);

        return ResponseEntity.ok("Hai abbandonato il team con successo.");
    }
}