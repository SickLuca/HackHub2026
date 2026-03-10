package it.unicam.cs.ids.services.abstractions;

import it.unicam.cs.ids.dtos.CreateHackathonDTO;
import it.unicam.cs.ids.dtos.HackathonResponseDTO;
import it.unicam.cs.ids.models.Hackathon;

import java.util.List;

public interface IHackathonService {
    Hackathon addHackathon(CreateHackathonDTO hackathon);
    Hackathon updateHackathon(Hackathon hackathon);
    Hackathon deleteHackathon(Long id);
    Hackathon getHackathonById(Long id);
    List<HackathonResponseDTO> getAllHackathons();

}

