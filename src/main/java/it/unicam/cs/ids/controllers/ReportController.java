package it.unicam.cs.ids.controllers;

import it.unicam.cs.ids.dtos.requests.CreateReportDTO;
import it.unicam.cs.ids.dtos.requests.UpdateReportDTO;
import it.unicam.cs.ids.dtos.responses.ReportResponseDTO;
import it.unicam.cs.ids.security.SecurityUtils;
import it.unicam.cs.ids.services.abstractions.IReportService;
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
 * Controller REST per la gestione dei report/segnalazioni.
 * <p>
 * Permette ai mentori di creare report relativi ai team seguiti
 * e agli organizzatori di consultarli e rispondere con azioni correttive.
 * </p>
 */
@RestController
@RequestMapping("/api/reports")
@Validated
public class ReportController {

    private final IReportService reportService;

    public ReportController(IReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Crea un nuovo report su un team all'interno di un hackathon.
     * <p>Accessibile solo agli utenti con ruolo {@code MENTOR}.
     * Il mentore deve essere assegnato all'hackathon di riferimento.</p>
     *
     * @param request DTO contenente i dettagli del report (hackathon, team, descrizione)
     * @return {@link ReportResponseDTO} con i dettagli del report appena creato
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<ReportResponseDTO> createReport(@Valid @RequestBody CreateReportDTO request) {
        Long mentorId = SecurityUtils.getAuthenticatedUserId();

        ReportResponseDTO response = reportService.createReport(request, mentorId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Restituisce tutti i report associati a un determinato hackathon.
     * <p>Accessibile solo agli utenti con ruolo {@code ORGANIZER}.
     * L'organizzatore deve essere il proprietario dell'hackathon.</p>
     *
     * @param hackathonId l'ID dell'hackathon di cui recuperare i report
     * @return lista di {@link ReportResponseDTO} con i report dell'hackathon specificato
     */
    @GetMapping("/getAll")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<List<ReportResponseDTO>> getReportsForHackathon(@RequestParam
                                                                              @NotNull(message = "L'ID deve essere maggiore di 0")
                                                                              @Positive(message = "L'ID dell'hackathon deve essere un numero positivo")
                                                                              Long hackathonId) {
        Long organizerId = SecurityUtils.getAuthenticatedUserId();

        List<ReportResponseDTO> response = reportService.getReportsForHackathon(hackathonId, organizerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Risponde a un report esistente con un'azione da parte dell'organizzatore.
     * <p>Accessibile solo agli utenti con ruolo {@code ORGANIZER}.
     * Permette di aggiornare lo stato del report e aggiungere una risposta.</p>
     *
     * @param request DTO contenente l'ID del report, la risposta e il nuovo stato
     * @return {@link ReportResponseDTO} con lo stato aggiornato del report
     */
    @PostMapping("/respond")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<ReportResponseDTO> respondToReport(@Valid @RequestBody UpdateReportDTO request) {
        Long organizerId = SecurityUtils.getAuthenticatedUserId();

        ReportResponseDTO response = reportService.respondToReport(request, organizerId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}