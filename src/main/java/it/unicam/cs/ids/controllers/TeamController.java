package it.unicam.cs.ids.controllers;

import it.unicam.cs.ids.dtos.requests.CreateTeamDTO;
import it.unicam.cs.ids.dtos.requests.SubscribeTeamDTO;
import it.unicam.cs.ids.dtos.responses.TeamResponseDTO;
import it.unicam.cs.ids.security.SecurityUtils;
import it.unicam.cs.ids.services.abstractions.ITeamService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<TeamResponseDTO> createTeam(@RequestBody CreateTeamDTO request) {
        Long userId = SecurityUtils.getAuthenticatedUserId();

        TeamResponseDTO response = teamService.createTeam(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/subscribe")
    @PreAuthorize("hasRole('TEAM_LEADER')")
    public ResponseEntity<TeamResponseDTO> subscribeToHackathon(@RequestBody SubscribeTeamDTO request) {
        Long leaderId = SecurityUtils.getAuthenticatedUserId();

        TeamResponseDTO response = teamService.subscribeToHackathon(request, leaderId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}