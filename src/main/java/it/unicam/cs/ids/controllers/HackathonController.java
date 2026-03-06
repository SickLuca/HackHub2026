package it.unicam.cs.ids.controllers;

import it.unicam.cs.ids.dtos.CreateHackathonDTO;
import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.services.HackathonService;

public class HackathonController {

    private HackathonService hackathonService;

    private HackathonController(HackathonService hackathonService) {
        this.hackathonService = hackathonService;
    }

    public Hackathon createHackathon(CreateHackathonDTO request) {
        return hackathonService.addHackathon(request);
    }

}
