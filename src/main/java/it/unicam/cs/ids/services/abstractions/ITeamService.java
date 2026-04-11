package it.unicam.cs.ids.services.abstractions;

import it.unicam.cs.ids.dtos.requests.CreateTeamDTO;
import it.unicam.cs.ids.dtos.requests.SubscribeTeamDTO;
import it.unicam.cs.ids.dtos.responses.TeamResponseDTO;

public interface ITeamService {
    TeamResponseDTO createTeam(CreateTeamDTO request, Long creatorId);

    TeamResponseDTO subscribeToHackathon(SubscribeTeamDTO request, Long leaderId);
    
    TeamResponseDTO getTeamByCurrentUser(Long userId);

    void leaveTeam(Long userId);
}