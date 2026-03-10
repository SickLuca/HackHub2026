package it.unicam.cs.ids.controllers;

import it.unicam.cs.ids.dtos.CreateTeamDTO;
import it.unicam.cs.ids.models.Team;
import it.unicam.cs.ids.services.abstractions.ITeamService;

public class TeamController {

    private final ITeamService teamService;

    public TeamController(ITeamService teamService) {
        this.teamService = teamService;
    }

    public Team createTeam(CreateTeamDTO request) {
        return teamService.createTeam(request);
    }
}