package it.unicam.cs.ids.services.abstractions;

import it.unicam.cs.ids.dtos.requests.CreateTeamDTO;
import it.unicam.cs.ids.dtos.requests.SubscribeTeamDTO;
import it.unicam.cs.ids.dtos.responses.TeamResponseDTO;

public interface ITeamService {
    TeamResponseDTO createTeam(CreateTeamDTO request);

    TeamResponseDTO subscribeToHackathon(SubscribeTeamDTO request);
}