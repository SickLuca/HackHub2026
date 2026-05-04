package it.unicam.cs.ids.controllers;

import it.unicam.cs.ids.dtos.requests.CreateTeamDTO;
import it.unicam.cs.ids.dtos.requests.SubscribeTeamDTO;
import it.unicam.cs.ids.dtos.responses.TeamResponseDTO;
import it.unicam.cs.ids.security.SecurityUtils;
import it.unicam.cs.ids.services.abstractions.ITeamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST per la gestione dei team.
 * <p>
 * Fornisce endpoint per la creazione di team, l'iscrizione a hackathon,
 * la visualizzazione del proprio team e l'abbandono del team.
 * </p>
 */
@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final ITeamService teamService;

    public TeamController(ITeamService teamService) {
        this.teamService = teamService;
    }

    /**
     * Crea un nuovo team con l'utente autenticato come team leader.
     * <p>Accessibile solo agli utenti con ruolo {@code USER_NO_TEAM}.</p>
     *
     * @param request DTO contenente il nome del team
     * @return {@link TeamResponseDTO} con i dettagli del team appena creato
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('USER_NO_TEAM')")
    public ResponseEntity<TeamResponseDTO> createTeam(@Valid @RequestBody CreateTeamDTO request) {
        Long userId = SecurityUtils.getAuthenticatedUserId();

        TeamResponseDTO response = teamService.createTeam(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Iscrive il team del leader a un hackathon.
     * <p>Accessibile solo agli utenti con ruolo {@code TEAM_LEADER}.
     * L'hackathon deve essere aperto alle iscrizioni.</p>
     *
     * @param request DTO contenente l'ID dell'hackathon a cui iscriversi
     * @return {@link TeamResponseDTO} aggiornato con l'hackathon associato
     */
    @PostMapping("/subscribe")
    @PreAuthorize("hasRole('TEAM_LEADER')")
    public ResponseEntity<TeamResponseDTO> subscribeToHackathon(@Valid @RequestBody SubscribeTeamDTO request) {
        Long leaderId = SecurityUtils.getAuthenticatedUserId();

        TeamResponseDTO response = teamService.subscribeToHackathon(request, leaderId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Restituisce le informazioni del team dell'utente autenticato.
     * <p>Accessibile agli utenti con ruolo {@code USER_NO_TEAM}, {@code TEAM_MEMBER} o {@code TEAM_LEADER}.</p>
     *
     * @return {@link TeamResponseDTO} con i dettagli del team corrente
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER_NO_TEAM', 'TEAM_MEMBER', 'TEAM_LEADER')")
    public ResponseEntity<TeamResponseDTO> getMyTeam() {
        Long userId = SecurityUtils.getAuthenticatedUserId();

        TeamResponseDTO response = teamService.getTeamByCurrentUser(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Permette all'utente autenticato di abbandonare il proprio team.
     * <p>Accessibile agli utenti con ruolo {@code TEAM_MEMBER} o {@code TEAM_LEADER}.
     * Se il leader abbandona, il team potrebbe essere sciolto.</p>
     *
     * @return messaggio di conferma dell'abbandono
     */
    @PostMapping("/leave")
    @PreAuthorize("hasAnyRole('TEAM_MEMBER', 'TEAM_LEADER')")
    public ResponseEntity<String> leaveTeam() {
        Long userId = SecurityUtils.getAuthenticatedUserId();

        teamService.leaveTeam(userId);

        return ResponseEntity.ok("Hai abbandonato il team con successo.");
    }
}