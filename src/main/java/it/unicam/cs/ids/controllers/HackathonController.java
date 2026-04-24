package it.unicam.cs.ids.controllers;

import it.unicam.cs.ids.dtos.requests.AddMentorDTO;
import it.unicam.cs.ids.dtos.requests.CreateHackathonDTO;
import it.unicam.cs.ids.dtos.requests.ProclaimWinnerDTO;
import it.unicam.cs.ids.dtos.responses.HackathonPublicResponseDTO;
import it.unicam.cs.ids.dtos.responses.HackathonResponseDTO;
import it.unicam.cs.ids.security.SecurityUtils;
import it.unicam.cs.ids.services.abstractions.IHackathonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hackathon")
public class HackathonController {

    private final IHackathonService hackathonService;

    public HackathonController(IHackathonService hackathonService) {
        this.hackathonService = hackathonService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<HackathonResponseDTO> createHackathon(@Valid @RequestBody CreateHackathonDTO request) {
        Long organizerId = SecurityUtils.getAuthenticatedUserId();
        HackathonResponseDTO response = hackathonService.addHackathon(request,organizerId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/getAll")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<HackathonResponseDTO>> getAllHackathons() {
        List<HackathonResponseDTO> response = hackathonService.getAllHackathons();
        // Restituisce la lista con il codice HTTP 200 (OK)
        return ResponseEntity.ok(response);
    }

    @PostMapping("/addMentors")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<HackathonResponseDTO> addMentorToHackathon(@Valid @RequestBody AddMentorDTO request) {
        Long organizerId = SecurityUtils.getAuthenticatedUserId();
        HackathonResponseDTO response = hackathonService.addMentorToHackathon(request, organizerId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/proclaimWinner")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<HackathonResponseDTO> proclaimWinner(@Valid @RequestBody ProclaimWinnerDTO request) {
        Long organizerId = SecurityUtils.getAuthenticatedUserId();

        HackathonResponseDTO response = hackathonService.proclaimWinner(request, organizerId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/getPublicInfo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<HackathonPublicResponseDTO>> getPublicHackathons() {
        return ResponseEntity.ok(hackathonService.getHackathonsPublicInfo());
    }

}
