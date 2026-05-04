package it.unicam.cs.ids.controllers;

import it.unicam.cs.ids.dtos.requests.CreateSubmissionDTO;
import it.unicam.cs.ids.dtos.requests.EvaluateSubmissionDTO;
import it.unicam.cs.ids.dtos.responses.SubmissionResponseDTO;
import it.unicam.cs.ids.dtos.requests.UpdateSubmissionDTO;
import it.unicam.cs.ids.security.SecurityUtils;
import it.unicam.cs.ids.services.abstractions.ISubmissionService;
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
 * Controller REST per la gestione delle submission (consegne dei progetti).
 * <p>
 * Gestisce l'intero ciclo di vita di una submission: dalla creazione,
 * all'aggiornamento, fino alla valutazione da parte dei giudici.
 * </p>
 */
@RestController
@RequestMapping("/api/submissions")
@Validated
public class SubmissionController {

    private final ISubmissionService submissionService;

    public SubmissionController(ISubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    /**
     * Invia una nuova submission per un hackathon.
     *
     * @param request DTO con i dati della submission
     * @return {@link SubmissionResponseDTO} della submission creata
     */
    @PostMapping("/submit")
    @PreAuthorize("hasAnyRole('TEAM_LEADER', 'TEAM_MEMBER')")
    public ResponseEntity<SubmissionResponseDTO> submitProject(@Valid @RequestBody CreateSubmissionDTO request) {
        Long userId = SecurityUtils.getAuthenticatedUserId();
        SubmissionResponseDTO response = submissionService.addSubmission(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Aggiorna una submission esistente prima della scadenza.
     *
     * @param request DTO con l'ID della submission e i campi da aggiornare
     * @return {@link SubmissionResponseDTO} aggiornata
     */
    @PostMapping("/update")
    @PreAuthorize("hasAnyRole('TEAM_LEADER', 'TEAM_MEMBER')")
    public ResponseEntity<SubmissionResponseDTO> updateSubmission(@Valid @RequestBody UpdateSubmissionDTO request) {
        Long userId = SecurityUtils.getAuthenticatedUserId();
        SubmissionResponseDTO response = submissionService.updateSubmission(request, userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Valuta una submission assegnando punteggio e feedback.
     * <p>Accessibile solo ai {@code JUDGE}.</p>
     *
     * @param request DTO con l'ID della submission, il punteggio e il feedback
     * @return {@link SubmissionResponseDTO} con la valutazione
     */
    @PostMapping("/evaluate")
    @PreAuthorize("hasRole('JUDGE')")
    public ResponseEntity<SubmissionResponseDTO> evaluateSubmission(@Valid @RequestBody EvaluateSubmissionDTO request) {
        Long judgeId = SecurityUtils.getAuthenticatedUserId();
        SubmissionResponseDTO response = submissionService.evaluateSubmission(request, judgeId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Restituisce i dettagli di una specifica submission.
     *
     * @param submissionId l'ID della submission
     * @return {@link SubmissionResponseDTO} con i dettagli completi
     */
    @GetMapping("/getSubmissionDetail")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'JUDGE', 'MENTOR')")
    public ResponseEntity<SubmissionResponseDTO> getSubmissionDetails(@RequestParam
                                                                          @NotNull(message = "L'id deve essere maggiore di 0")
                                                                          @Positive(message = "L'id deve essere un numero positivo")
                                                                          Long submissionId) {
        Long staffId = SecurityUtils.getAuthenticatedUserId();
        SubmissionResponseDTO response = submissionService.getSubmissionDetails(submissionId, staffId);
        return ResponseEntity.ok(response);
    }

    /**
     * Restituisce tutte le submission di un hackathon.
     *
     * @param hackathonId l'ID dell'hackathon
     * @return lista di {@link SubmissionResponseDTO}
     */
    @GetMapping("/getAllByHackathon")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'JUDGE', 'MENTOR')")
    public ResponseEntity<List<SubmissionResponseDTO>> getSubmissionsByHackathon(@RequestParam
                                                                                     @NotNull(message = "L'id deve essere maggiore di 0")
                                                                                     @Positive(message = "L'id deve essere un numero positivo")
                                                                                     Long hackathonId) {
        Long staffId = SecurityUtils.getAuthenticatedUserId();
        List<SubmissionResponseDTO> response = submissionService.getSubmissionsByHackathon(hackathonId, staffId);
        return ResponseEntity.ok(response);
    }
}