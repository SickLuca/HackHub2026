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
 * REST controller for managing submissions (project deliveries).
 * <p>
 * Manages the entire lifecycle of a submission: from creation,
 * to updating, through to evaluation by judges.
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
     * Submits a new submission for a hackathon.
     *
     * @param request DTO with the submission data
     * @return {@link SubmissionResponseDTO} for the created submission
     */
    @PostMapping("/submit")
    @PreAuthorize("hasAnyRole('TEAM_LEADER', 'TEAM_MEMBER')")
    public ResponseEntity<SubmissionResponseDTO> submitProject(@Valid @RequestBody CreateSubmissionDTO request) {
        Long userId = SecurityUtils.getAuthenticatedUserId();
        SubmissionResponseDTO response = submissionService.addSubmission(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Updates an existing submission before the deadline.
     *
     * @param request DTO with the submission ID and fields to update
     * @return updated {@link SubmissionResponseDTO}
     */
    @PostMapping("/update")
    @PreAuthorize("hasAnyRole('TEAM_LEADER', 'TEAM_MEMBER')")
    public ResponseEntity<SubmissionResponseDTO> updateSubmission(@Valid @RequestBody UpdateSubmissionDTO request) {
        Long userId = SecurityUtils.getAuthenticatedUserId();
        SubmissionResponseDTO response = submissionService.updateSubmission(request, userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Evaluates a submission by assigning a score and feedback.
     * <p>Accessible only to {@code JUDGE} users.</p>
     *
     * @param request DTO with the submission ID, the score, and the feedback
     * @return {@link SubmissionResponseDTO} with the evaluation
     */
    @PostMapping("/evaluate")
    @PreAuthorize("hasRole('JUDGE')")
    public ResponseEntity<SubmissionResponseDTO> evaluateSubmission(@Valid @RequestBody EvaluateSubmissionDTO request) {
        Long judgeId = SecurityUtils.getAuthenticatedUserId();
        SubmissionResponseDTO response = submissionService.evaluateSubmission(request, judgeId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Returns the details of a specific submission.
     *
     * @param submissionId the ID of the submission
     * @return {@link SubmissionResponseDTO} with the full details
     */
    @GetMapping("/getSubmissionDetail")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'JUDGE', 'MENTOR')")
    public ResponseEntity<SubmissionResponseDTO> getSubmissionDetails(@RequestParam
                                                                          @NotNull(message = "The ID must be greater than 0")
                                                                          @Positive(message = "The ID must be a positive number")
                                                                          Long submissionId) {
        Long staffId = SecurityUtils.getAuthenticatedUserId();
        SubmissionResponseDTO response = submissionService.getSubmissionDetails(submissionId, staffId);
        return ResponseEntity.ok(response);
    }

    /**
     * Returns all submissions for a hackathon.
     *
     * @param hackathonId the ID of the hackathon
     * @return list of {@link SubmissionResponseDTO}
     */
    @GetMapping("/getAllByHackathon")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'JUDGE', 'MENTOR')")
    public ResponseEntity<List<SubmissionResponseDTO>> getSubmissionsByHackathon(@RequestParam
                                                                                     @NotNull(message = "The ID must be greater than 0")
                                                                                     @Positive(message = "The ID must be a positive number")
                                                                                     Long hackathonId) {
        Long staffId = SecurityUtils.getAuthenticatedUserId();
        List<SubmissionResponseDTO> response = submissionService.getSubmissionsByHackathon(hackathonId, staffId);
        return ResponseEntity.ok(response);
    }
}