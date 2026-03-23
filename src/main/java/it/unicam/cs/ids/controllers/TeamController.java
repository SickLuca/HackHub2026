package it.unicam.cs.ids.controllers;

import it.unicam.cs.ids.dtos.requests.CreateTeamDTO;
import it.unicam.cs.ids.dtos.requests.SubscribeTeamDTO;
import it.unicam.cs.ids.dtos.responses.TeamResponseDTO;
import it.unicam.cs.ids.services.abstractions.ITeamService;

public class TeamController {

    private final ITeamService teamService;

    public TeamController(ITeamService teamService) {
        this.teamService = teamService;
    }

    public TeamResponseDTO createTeam(CreateTeamDTO request) {
        return teamService.createTeam(request);
    }

    public TeamResponseDTO subscribeToHackathon(SubscribeTeamDTO request) {
        return teamService.subscribeToHackathon(request);
    }
}