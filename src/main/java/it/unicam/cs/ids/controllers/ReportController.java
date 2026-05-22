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
 * REST controller for managing reports/flagging.
 * <p>
 * Allows mentors to create reports on monitored teams
 * and organizers to review and respond with corrective actions.
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
     * Creates a new report on a team within a hackathon.
     * <p>Accessible only to users with the {@code MENTOR} role.
     * The mentor must be assigned to the referenced hackathon.</p>
     *
     * @param request DTO containing the report details (hackathon, team, description)
     * @return {@link ReportResponseDTO} with the details of the newly created report
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<ReportResponseDTO> createReport(@Valid @RequestBody CreateReportDTO request) {
        Long mentorId = SecurityUtils.getAuthenticatedUserId();

        ReportResponseDTO response = reportService.createReport(request, mentorId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Returns all reports associated with a specific hackathon.
     * <p>Accessible only to users with the {@code ORGANIZER} role.
     * The organizer must be the owner of the hackathon.</p>
     *
     * @param hackathonId the ID of the hackathon whose reports to retrieve
     * @return list of {@link ReportResponseDTO} with the reports for the specified hackathon
     */
    @GetMapping("/getAll")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<List<ReportResponseDTO>> getReportsForHackathon(@RequestParam
                                                                              @NotNull(message = "The ID must be greater than 0")
                                                                              @Positive(message = "The hackathon ID must be a positive number")
                                                                              Long hackathonId) {
        Long organizerId = SecurityUtils.getAuthenticatedUserId();

        List<ReportResponseDTO> response = reportService.getReportsForHackathon(hackathonId, organizerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Responds to an existing report with an action from the organizer.
     * <p>Accessible only to users with the {@code ORGANIZER} role.
     * Allows updating the status of the report and adding a response.</p>
     *
     * @param request DTO containing the report ID, the response, and the new status
     * @return {@link ReportResponseDTO} with the updated report status
     */
    @PostMapping("/respond")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<ReportResponseDTO> respondToReport(@Valid @RequestBody UpdateReportDTO request) {
        Long organizerId = SecurityUtils.getAuthenticatedUserId();

        ReportResponseDTO response = reportService.respondToReport(request, organizerId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}