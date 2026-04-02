package it.unicam.cs.ids.controllers;

import it.unicam.cs.ids.dtos.requests.CreateReportDTO;
import it.unicam.cs.ids.dtos.requests.UpdateReportDTO;
import it.unicam.cs.ids.dtos.responses.ReportResponseDTO;
import it.unicam.cs.ids.security.SecurityUtils;
import it.unicam.cs.ids.services.abstractions.IReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final IReportService reportService;

    public ReportController(IReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<ReportResponseDTO> createReport(@RequestBody CreateReportDTO request) {
        Long mentorId = SecurityUtils.getAuthenticatedUserId();

        ReportResponseDTO response = reportService.createReport(request, mentorId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/getAll")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<List<ReportResponseDTO>> getReportsForHackathon(@RequestParam Long hackathonId) {
        Long organizerId = SecurityUtils.getAuthenticatedUserId();

        List<ReportResponseDTO> response = reportService.getReportsForHackathon(hackathonId, organizerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/respond")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<ReportResponseDTO> respondToReport(@RequestBody UpdateReportDTO request) {
        Long organizerId = SecurityUtils.getAuthenticatedUserId();

        ReportResponseDTO response = reportService.respondToReport(request, organizerId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}