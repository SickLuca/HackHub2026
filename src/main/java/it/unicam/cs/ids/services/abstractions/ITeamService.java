package it.unicam.cs.ids.services.abstractions;

import it.unicam.cs.ids.dtos.CreateTeamDTO;
import it.unicam.cs.ids.dtos.SubscribeTeamDTO;
import it.unicam.cs.ids.dtos.TeamResponseDTO;
import it.unicam.cs.ids.models.Team;

public interface ITeamService {
    TeamResponseDTO createTeam(CreateTeamDTO request);

    TeamResponseDTO subscribeToHackathon(SubscribeTeamDTO request);
}