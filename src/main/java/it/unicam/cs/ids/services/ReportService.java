package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.requests.CreateReportDTO;
import it.unicam.cs.ids.dtos.requests.UpdateReportDTO;
import it.unicam.cs.ids.dtos.responses.ReportResponseDTO;
import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.models.Report;
import it.unicam.cs.ids.models.StaffUser;
import it.unicam.cs.ids.models.Team;
import it.unicam.cs.ids.models.utils.ReportStatus;
import it.unicam.cs.ids.services.abstractions.IReportService;
import it.unicam.cs.ids.utils.unitOfWork.IUnitOfWork;
import it.unicam.cs.ids.validators.abstractions.Validator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ReportService implements IReportService {

    private final IUnitOfWork unitOfWork;
    private final Validator<CreateReportDTO> validator;

    public ReportService(IUnitOfWork unitOfWork, Validator<CreateReportDTO> validator) {
        this.unitOfWork = unitOfWork;
        this.validator = validator;
    }

    @Override
    public ReportResponseDTO createReport(CreateReportDTO request) {
        // 1. Validazione
        validator.validate(request);

        // 2. Recupero entità (già sicure grazie al validatore)
        StaffUser mentor = unitOfWork.getStaffUserRepository().getById(request.mentorId());
        Team team = unitOfWork.getTeamRepository().getById(request.teamId());
        Hackathon hackathon = unitOfWork.getHackathonRepository().getById(request.hackathonId());

        // 3. Creazione entità
        Report report = new Report();
        report.setMentor(mentor);
        report.setTeam(team);
        report.setHackathon(hackathon);
        report.setDescription(request.description());
        report.setStatus(ReportStatus.PENDING);
        report.setCreatedAt(LocalDateTime.now());
        report.setDecisionNote("N/D");

        // 4. Salvataggio
        unitOfWork.getReportRepository().create(report);

        // 5. Ritorno DTO
        return mapToDTO(report);
    }

    @Override
    public List<ReportResponseDTO> getReportsForHackathon(Long hackathonId, Long organizerId) {
        Hackathon hackathon = unitOfWork.getHackathonRepository().getById(hackathonId);
        if (hackathon == null) throw new IllegalArgumentException("Hackathon non trovato");

        // Solo l'organizzatore dell'hackathon può vedere i report
        if (!hackathon.getOrganizer().getId().equals(organizerId)) {
            throw new SecurityException("Solo l'organizzatore può visualizzare le segnalazioni di questo hackathon.");
        }

        List<Report> reports = unitOfWork.getReportRepository().getReportsByHackathonId(hackathonId);

        return reports.stream()
                .map(this::mapToDTO).
                collect(Collectors.toList());
    }

    @Override
    public ReportResponseDTO respondToReport(UpdateReportDTO request) {
        Report report = unitOfWork.getReportRepository().getById(request.reportId());
        if (report == null) throw new IllegalArgumentException("Segnalazione non trovata");

        if (!report.getHackathon().getOrganizer().getId().equals(request.organizerId())) {
            throw new SecurityException("Solo l'organizzatore può aggiornare lo stato di questa segnalazione.");
        }

        if (report.getDecisionNote().isEmpty()) {
            throw new IllegalArgumentException("La decisione deve essere specificata prima di aggiornare lo stato della segnalazione.");
        }

        if (report.getStatus() != ReportStatus.PENDING) {
            throw new IllegalStateException("La segnalazione è già stata gestita");
        }

        report.setDecisionNote(request.decisionNote());
        report.setStatus(ReportStatus.RESOLVED);
        unitOfWork.getReportRepository().update(report);

        return mapToDTO(report);
    }

    private ReportResponseDTO mapToDTO(Report r) {
        return new ReportResponseDTO(
                r.getId(),
                r.getMentor().getName() + " " + r.getMentor().getSurname(),
                r.getTeam().getName(),
                r.getHackathon().getName(),
                r.getDescription(),
                r.getStatus(),
                r.getCreatedAt(),
                r.getDecisionNote()
        );
    }
}