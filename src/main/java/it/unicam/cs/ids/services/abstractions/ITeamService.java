package it.unicam.cs.ids.services.abstractions;

import it.unicam.cs.ids.dtos.CreateTeamDTO;
import it.unicam.cs.ids.dtos.SubscribeTeamDTO;
import it.unicam.cs.ids.models.Team;

public interface ITeamService {
    Team createTeam(CreateTeamDTO request);

    Team subscribeToHackathon(SubscribeTeamDTO request);
}