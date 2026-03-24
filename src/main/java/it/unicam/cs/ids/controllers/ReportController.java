package it.unicam.cs.ids.controllers;

import it.unicam.cs.ids.dtos.requests.CreateReportDTO;
import it.unicam.cs.ids.dtos.requests.UpdateReportDTO;
import it.unicam.cs.ids.dtos.responses.ReportResponseDTO;
import it.unicam.cs.ids.services.abstractions.IReportService;

import java.util.List;

public class ReportController {

    private final IReportService reportService;

    public ReportController(IReportService reportService) {
        this.reportService = reportService;
    }

    public ReportResponseDTO createReport(CreateReportDTO request) {
        return reportService.createReport(request);
    }

    public List<ReportResponseDTO> getReportsForHackathon(Long hackathonId, Long organizerId) {
        return reportService.getReportsForHackathon(hackathonId, organizerId);
    }

    public ReportResponseDTO respondToReport(UpdateReportDTO request) {
        return reportService.respondToReport(request);
    }
}