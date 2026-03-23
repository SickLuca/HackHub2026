package it.unicam.cs.ids.controllers;

import it.unicam.cs.ids.dtos.requests.CreateHackathonDTO;
import it.unicam.cs.ids.dtos.responses.HackathonResponseDTO;
import it.unicam.cs.ids.services.abstractions.IHackathonService;

import java.util.List;

public class HackathonController {

    private IHackathonService hackathonService;

    public HackathonController(IHackathonService hackathonService) {
        this.hackathonService = hackathonService;
    }

    public HackathonResponseDTO createHackathon(CreateHackathonDTO request) {
        return hackathonService.addHackathon(request);
    }

    public List<HackathonResponseDTO> getAllHackathon() {
        return hackathonService.getAllHackathons();
    }
}
