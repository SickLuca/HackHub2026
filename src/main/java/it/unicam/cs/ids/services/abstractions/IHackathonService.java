package it.unicam.cs.ids.services.abstractions;

import it.unicam.cs.ids.dtos.requests.AddMentorDTO;
import it.unicam.cs.ids.dtos.requests.CreateHackathonDTO;
import it.unicam.cs.ids.dtos.responses.HackathonResponseDTO;
import it.unicam.cs.ids.models.Hackathon;

import java.util.List;

public interface IHackathonService {
    HackathonResponseDTO addHackathon(CreateHackathonDTO hackathon,Long organizerId);
    HackathonResponseDTO updateHackathon(Hackathon hackathon);
    HackathonResponseDTO deleteHackathon(Long id);
    HackathonResponseDTO getHackathonById(Long id);
    List<HackathonResponseDTO> getAllHackathons();
    HackathonResponseDTO addMentorToHackathon(AddMentorDTO request, Long organizerId);
}

