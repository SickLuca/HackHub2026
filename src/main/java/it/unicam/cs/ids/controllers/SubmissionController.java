package it.unicam.cs.ids.controllers;

import it.unicam.cs.ids.dtos.requests.CreateSubmissionDTO;
import it.unicam.cs.ids.dtos.requests.EvaluateSubmissionDTO;
import it.unicam.cs.ids.dtos.responses.SubmissionResponseDTO;
import it.unicam.cs.ids.dtos.requests.UpdateSubmissionDTO;
import it.unicam.cs.ids.security.SecurityUtils;
import it.unicam.cs.ids.services.abstractions.ISubmissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private final ISubmissionService submissionService;

    // Costruttore per l'iniezione delle dipendenze (uguale al TeamController)
    public SubmissionController(ISubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    // Metodo per il Membro del Team per inviare il progetto la prima volta
    @PostMapping("/submit")
    @PreAuthorize("hasAnyRole('TEAM_LEADER', 'TEAM_MEMBER')")
    public ResponseEntity<SubmissionResponseDTO> submitProject(@RequestBody CreateSubmissionDTO request) {
        Long userId = SecurityUtils.getAuthenticatedUserId();

        SubmissionResponseDTO response = submissionService.addSubmission(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);

    }

    //Metodo per il Membro del Team per aggiornare il progetto prima della scadenza
    @PostMapping("/update")
    @PreAuthorize("hasAnyRole('TEAM_LEADER', 'TEAM_MEMBER')")
    public ResponseEntity<SubmissionResponseDTO> updateSubmission(@RequestBody UpdateSubmissionDTO request) {
        Long userId = SecurityUtils.getAuthenticatedUserId();

        SubmissionResponseDTO response = submissionService.updateSubmission(request, userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //Metodo per il Giudice per valutare una sottomissione
    @PostMapping("/evaluate")
    @PreAuthorize("hasRole('JUDGE')")
    public ResponseEntity<SubmissionResponseDTO> evaluateSubmission(@RequestBody EvaluateSubmissionDTO request) {
        Long judgeId = SecurityUtils.getAuthenticatedUserId();

        SubmissionResponseDTO response = submissionService.evaluateSubmission(request, judgeId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getSubmissionDetail")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'JUDGE', 'MENTOR')")
    public ResponseEntity<SubmissionResponseDTO> getSubmissionDetails(@RequestParam Long submissionId) {
        Long staffId = SecurityUtils.getAuthenticatedUserId();

        SubmissionResponseDTO response = submissionService.getSubmissionDetails(submissionId, staffId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getAllByHackathon")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'JUDGE', 'MENTOR')")
    public ResponseEntity<List<SubmissionResponseDTO>> getSubmissionsByHackathon(@RequestParam Long hackathonId) {
        Long staffId = SecurityUtils.getAuthenticatedUserId();

        List<SubmissionResponseDTO> response = submissionService.getSubmissionsByHackathon(hackathonId, staffId);
        return ResponseEntity.ok(response);
    }
}