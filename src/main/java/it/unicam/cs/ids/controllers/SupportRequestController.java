package it.unicam.cs.ids.controllers;

import it.unicam.cs.ids.dtos.requests.CreateSupportRequestDTO;
import it.unicam.cs.ids.dtos.requests.ScheduleCallDTO;
import it.unicam.cs.ids.dtos.responses.SupportRequestResponseDTO;
import it.unicam.cs.ids.security.SecurityUtils;
import it.unicam.cs.ids.services.abstractions.ISupportRequestService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST per la gestione delle richieste di supporto.
 * <p>
 * Permette ai membri dei team di creare richieste di aiuto
 * e ai mentori di visualizzarle e pianificare chiamate di supporto.
 * </p>
 */
@RestController
@RequestMapping("/api/supportRequest")
@Validated
public class SupportRequestController {
    private final ISupportRequestService supportRequestService;

    public SupportRequestController(ISupportRequestService supportRequestService) {
        this.supportRequestService = supportRequestService;
    }

    /**
     * Crea una nuova richiesta di supporto per il team dell'utente.
     * <p>Accessibile agli utenti con ruolo {@code TEAM_LEADER} o {@code TEAM_MEMBER}.</p>
     *
     * @param request DTO contenente il messaggio e l'hackathon di riferimento
     * @return {@link SupportRequestResponseDTO} con i dettagli della richiesta creata
     */
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('TEAM_LEADER', 'TEAM_MEMBER')")
    public ResponseEntity<SupportRequestResponseDTO> createSupportRequest(@Valid @RequestBody CreateSupportRequestDTO request) {
        Long userId = SecurityUtils.getAuthenticatedUserId();

        SupportRequestResponseDTO response = supportRequestService.createRequest(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Restituisce tutte le richieste di supporto per un hackathon.
     * <p>Accessibile solo agli utenti con ruolo {@code MENTOR}.
     * Il mentore deve essere assegnato all'hackathon specificato.</p>
     *
     * @param hackathonId l'ID dell'hackathon di cui recuperare le richieste
     * @return lista di {@link SupportRequestResponseDTO}
     */
    @GetMapping("/getAll")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<List<SupportRequestResponseDTO>> getRequestsForHackathon(@RequestParam
                                                                                       @NotNull(message = "L'id deve essere maggiore di 0")
                                                                                       @Positive(message = "L'id deve essere un numero positivo")
                                                                                       Long hackathonId) {
        Long mentorId = SecurityUtils.getAuthenticatedUserId();

        List<SupportRequestResponseDTO> response = supportRequestService.getRequestsForHackathon(hackathonId, mentorId);
        return ResponseEntity.ok(response);
    }

    /**
     * Pianifica una chiamata di supporto per una richiesta esistente.
     * <p>Accessibile solo agli utenti con ruolo {@code MENTOR}.
     * Genera un link per la videochiamata tramite il servizio calendario integrato (Adapter pattern).</p>
     *
     * @param request DTO contenente l'ID della richiesta e i dettagli della chiamata
     * @return {@link SupportRequestResponseDTO} con il link alla chiamata pianificata
     */
    @PostMapping("/scheduleCall")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<SupportRequestResponseDTO> scheduleCall(@Valid @RequestBody ScheduleCallDTO request) {
        Long mentorId = SecurityUtils.getAuthenticatedUserId();

        SupportRequestResponseDTO response = supportRequestService.scheduleCall(request, mentorId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}