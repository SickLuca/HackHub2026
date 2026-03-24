package it.unicam.cs.ids.services.abstractions;

import it.unicam.cs.ids.dtos.requests.CreateReportDTO;
import it.unicam.cs.ids.dtos.requests.UpdateReportDTO;
import it.unicam.cs.ids.dtos.responses.ReportResponseDTO;

import java.util.List;

public interface IReportService {
    ReportResponseDTO createReport(CreateReportDTO request);

    List<ReportResponseDTO> getReportsForHackathon(Long hackathonId, Long organizerId);

    ReportResponseDTO respondToReport(UpdateReportDTO request);
}