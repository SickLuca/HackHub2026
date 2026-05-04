package it.unicam.cs.ids.controllers;

import it.unicam.cs.ids.dtos.requests.CreateInvitationDTO;
import it.unicam.cs.ids.dtos.responses.InvitationResponseDTO;
import it.unicam.cs.ids.dtos.requests.RespondInvitationDTO;
import it.unicam.cs.ids.security.SecurityUtils;
import it.unicam.cs.ids.services.abstractions.IInvitationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST per la gestione degli inviti ai team.
 * <p>
 * Permette ai team leader di inviare inviti ad altri utenti,
 * agli utenti di visualizzare i propri inviti ricevuti
 * e di accettarli o rifiutarli.
 * </p>
 */
@RestController
@RequestMapping("/api/invitations")
public class InvitationController {

    private final IInvitationService invitationService;

    public InvitationController(IInvitationService invitationService) {
        this.invitationService = invitationService;
    }

    /**
     * Invia un invito a un utente per unirsi al team del mittente.
     * <p>Accessibile solo agli utenti con ruolo {@code TEAM_LEADER}.</p>
     *
     * @param request DTO contenente l'ID dell'utente da invitare e l'ID dell'hackathon di riferimento
     * @return {@link InvitationResponseDTO} con i dettagli dell'invito creato
     */
    @PostMapping("/send")
    @PreAuthorize("hasRole('TEAM_LEADER')")
    public ResponseEntity<InvitationResponseDTO> sendInvitation(@Valid @RequestBody CreateInvitationDTO request) {
        Long fromTeamLeaderId = SecurityUtils.getAuthenticatedUserId();

        InvitationResponseDTO response = invitationService.sendInvitation(request,fromTeamLeaderId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Restituisce tutti gli inviti ricevuti dall'utente autenticato.
     * <p>Accessibile agli utenti con ruolo {@code USER_NO_TEAM}, {@code TEAM_MEMBER} o {@code TEAM_LEADER}.</p>
     *
     * @return lista di {@link InvitationResponseDTO} con tutti gli inviti dell'utente corrente
     */
    @GetMapping("/getAll")
    @PreAuthorize("hasAnyRole('USER_NO_TEAM', 'TEAM_MEMBER', 'TEAM_LEADER')")
    public ResponseEntity<List<InvitationResponseDTO>> getMyInvitations(){
        Long userId = SecurityUtils.getAuthenticatedUserId();

        List<InvitationResponseDTO> response = invitationService.getAllInvitationsByCurrentUser(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Risponde a un invito ricevuto, accettandolo o rifiutandolo.
     * <p>Accessibile solo agli utenti con ruolo {@code USER_NO_TEAM}.
     * Se accettato, l'utente verrà aggiunto al team che ha inviato l'invito.</p>
     *
     * @param response DTO contenente l'ID dell'invito e la risposta (accettato/rifiutato)
     * @return {@link InvitationResponseDTO} con lo stato aggiornato dell'invito
     */
    @PostMapping("/respond")
    @PreAuthorize("hasRole('USER_NO_TEAM')")
    public ResponseEntity<InvitationResponseDTO> respondToInvitation(@Valid @RequestBody RespondInvitationDTO response) {
        Long userId = SecurityUtils.getAuthenticatedUserId();

        InvitationResponseDTO invitationResponse = invitationService.respondToInvitation(response, userId);
        return new ResponseEntity<>(invitationResponse, HttpStatus.OK);
    }

}