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

/**
 * Controller REST per la gestione degli hackathon.
 * <p>
 * Fornisce endpoint per la creazione, la consultazione,
 * l'assegnazione di mentori e la proclamazione del vincitore di un hackathon.
 * Le operazioni di scrittura sono riservate agli utenti con ruolo {@code ORGANIZER}.
 * </p>
 */
@RestController
@RequestMapping("/api/hackathon")
public class HackathonController {

    private final IHackathonService hackathonService;

    public HackathonController(IHackathonService hackathonService) {
        this.hackathonService = hackathonService;
    }

    /**
     * Crea un nuovo hackathon.
     * <p>Accessibile solo agli utenti con ruolo {@code ORGANIZER}.</p>
     *
     * @param request DTO con i dati dell'hackathon da creare (titolo, descrizione, date, ecc.)
     * @return {@link HackathonResponseDTO} con i dettagli dell'hackathon appena creato
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<HackathonResponseDTO> createHackathon(@Valid @RequestBody CreateHackathonDTO request) {
        Long organizerId = SecurityUtils.getAuthenticatedUserId();
        HackathonResponseDTO response = hackathonService.addHackathon(request,organizerId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Restituisce la lista completa di tutti gli hackathon presenti nel sistema.
     * <p>Accessibile a qualsiasi utente autenticato.</p>
     *
     * @return lista di {@link HackathonResponseDTO} con i dettagli completi di ogni hackathon
     */
    @GetMapping("/getAll")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<HackathonResponseDTO>> getAllHackathons() {
        List<HackathonResponseDTO> response = hackathonService.getAllHackathons();
        return ResponseEntity.ok(response);
    }

    /**
     * Aggiunge un mentore a un hackathon esistente.
     * <p>Accessibile solo agli utenti con ruolo {@code ORGANIZER}.
     * L'organizzatore deve essere il proprietario dell'hackathon.</p>
     *
     * @param request DTO contenente l'ID dell'hackathon e l'ID del mentore da aggiungere
     * @return {@link HackathonResponseDTO} aggiornato con il nuovo mentore
     */
    @PostMapping("/addMentors")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<HackathonResponseDTO> addMentorToHackathon(@Valid @RequestBody AddMentorDTO request) {
        Long organizerId = SecurityUtils.getAuthenticatedUserId();
        HackathonResponseDTO response = hackathonService.addMentorToHackathon(request, organizerId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Proclama il vincitore di un hackathon.
     * <p>Accessibile solo agli utenti con ruolo {@code ORGANIZER}.
     * L'hackathon deve essere nello stato appropriato per la proclamazione.</p>
     *
     * @param request DTO contenente l'ID dell'hackathon e l'ID del team vincitore
     * @return {@link HackathonResponseDTO} aggiornato con il vincitore proclamato
     */
    @PostMapping("/proclaimWinner")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<HackathonResponseDTO> proclaimWinner(@Valid @RequestBody ProclaimWinnerDTO request) {
        Long organizerId = SecurityUtils.getAuthenticatedUserId();

        HackathonResponseDTO response = hackathonService.proclaimWinner(request, organizerId);

        return ResponseEntity.ok(response);
    }

    /**
     * Restituisce le informazioni pubbliche di tutti gli hackathon.
     * <p>Espone un sottoinsieme ridotto di dati, adatto alla visualizzazione
     * in contesti dove non servono i dettagli completi (es. homepage).</p>
     *
     * @return lista di {@link HackathonPublicResponseDTO} con le informazioni pubbliche
     */
    @GetMapping("/getPublicInfo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<HackathonPublicResponseDTO>> getPublicHackathons() {
        return ResponseEntity.ok(hackathonService.getHackathonsPublicInfo());
    }

}
