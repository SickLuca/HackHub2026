package it.unicam.cs.ids.services.abstractions;

import it.unicam.cs.ids.dtos.CreateHackathonDTO;
import it.unicam.cs.ids.dtos.HackathonResponseDTO;
import it.unicam.cs.ids.models.Hackathon;

import java.util.List;

public interface IHackathonService {
    HackathonResponseDTO addHackathon(CreateHackathonDTO hackathon);
    HackathonResponseDTO updateHackathon(Hackathon hackathon);
    HackathonResponseDTO deleteHackathon(Long id);
    HackathonResponseDTO getHackathonById(Long id);
    List<HackathonResponseDTO> getAllHackathons();

}

