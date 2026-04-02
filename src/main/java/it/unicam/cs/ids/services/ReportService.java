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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReportService implements IReportService {

    private final IUnitOfWork unitOfWork;
    private final Validator<CreateReportDTO> validator;

    public ReportService(IUnitOfWork unitOfWork,
                         Validator<CreateReportDTO> validator) {

        this.unitOfWork = unitOfWork;
        this.validator = validator;
    }

    @Override
    public ReportResponseDTO createReport(CreateReportDTO request, Long mentorId) {
        // 1. Validazione
        validator.validate(request);

        // EXTRAIAMO L'ID DIRETTAMENTE DAL TOKEN JWT!

        // 2. Recupero entità (già sicure grazie al validatore)
        StaffUser mentor = unitOfWork.getStaffUserRepository().findById(mentorId).orElse(null);
        Team team = unitOfWork.getTeamRepository().findById(request.teamId()).orElse(null);
        Hackathon hackathon = unitOfWork.getHackathonRepository().findById(request.hackathonId()).orElse(null);

        if (hackathon == null) {
            throw new IllegalArgumentException("Hackathon not found");
        }

        // Controllo se il mentore è assegnato a questo hackathon
        boolean isMentorAssigned = hackathon.getMentors().stream()
                .anyMatch(m -> m.getId().equals(mentorId));
        if (!isMentorAssigned) {
            throw new SecurityException("Il mentore non è assegnato a questo hackathon e non può effettuare segnalazioni.");
        }

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
        unitOfWork.getReportRepository().save(report);

        //aggiorno la relazione bidirezionale in memoria
        hackathon.getReports().add(report); //dovremmo scrivere un add su hackathon?

        // 5. Ritorno DTO
        return mapToDTO(report);
    }

    @Override
    public List<ReportResponseDTO> getReportsForHackathon(Long hackathonId, Long organizerId) {

        Hackathon hackathon = unitOfWork.getHackathonRepository().findById(hackathonId).orElse(null);
        if (hackathon == null) throw new IllegalArgumentException("Hackathon non trovato");

        // Solo l'organizzatore dell'hackathon può vedere i report
        if (!hackathon.getOrganizer().getId().equals(organizerId)) {
            throw new SecurityException("Solo l'organizzatore può visualizzare le segnalazioni di questo hackathon.");
        }

        List<Report> reports = unitOfWork.getReportRepository().findByHackathonId(hackathonId);

        return reports.stream()
                .map(this::mapToDTO).
                collect(Collectors.toList());
    }

    @Override
    public ReportResponseDTO respondToReport(UpdateReportDTO request, Long organizerId) {

        Report report = unitOfWork.getReportRepository().findById(request.reportId()).orElse(null);
        if (report == null) throw new IllegalArgumentException("Segnalazione non trovata");

        if (!report.getHackathon().getOrganizer().getId().equals(organizerId)) {
            throw new SecurityException("Solo l'organizzatore può aggiornare lo stato di questa segnalazione.");
        }

        if (request.decisionNote() == null || request.decisionNote().isEmpty()) {
            throw new IllegalArgumentException("La decisione deve essere specificata prima di aggiornare lo stato della segnalazione.");
        }

        if (report.getStatus() != ReportStatus.PENDING) {
            throw new IllegalStateException("La segnalazione è già stata gestita");
        }

        report.setDecisionNote(request.decisionNote());
        report.setStatus(ReportStatus.RESOLVED);
        unitOfWork.getReportRepository().save(report);

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