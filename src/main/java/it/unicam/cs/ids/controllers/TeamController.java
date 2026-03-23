package it.unicam.cs.ids.controllers;

import it.unicam.cs.ids.dtos.CreateTeamDTO;
import it.unicam.cs.ids.dtos.SubscribeTeamDTO;
import it.unicam.cs.ids.dtos.TeamResponseDTO;
import it.unicam.cs.ids.models.Team;
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