package it.unicam.cs.ids.controllers;

import it.unicam.cs.ids.dtos.CreateHackathonDTO;
import it.unicam.cs.ids.dtos.HackathonResponseDTO;
import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.services.HackathonService;
import it.unicam.cs.ids.services.abstractions.IHackathonService;

import java.util.List;

public class HackathonController {

    private IHackathonService hackathonService;

    public HackathonController(IHackathonService hackathonService) {
        this.hackathonService = hackathonService;
    }

    public Hackathon createHackathon(CreateHackathonDTO request) {
        return hackathonService.addHackathon(request);
    }

    public List<HackathonResponseDTO> getAllHackathon() {
        return hackathonService.getAllHackathons();
    }
}
