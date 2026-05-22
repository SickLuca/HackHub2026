package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.requests.CreateReportDTO;
import it.unicam.cs.ids.dtos.requests.UpdateReportDTO;
import it.unicam.cs.ids.dtos.responses.ReportResponseDTO;
import it.unicam.cs.ids.exceptions.InvalidInputException;
import it.unicam.cs.ids.exceptions.ResourceNotFoundException;
import it.unicam.cs.ids.exceptions.RuleViolationException;
import it.unicam.cs.ids.exceptions.UnauthorizedActionException;
import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.models.Report;
import it.unicam.cs.ids.models.StaffUser;
import it.unicam.cs.ids.models.Team;
import it.unicam.cs.ids.models.utils.ReportStatus;
import it.unicam.cs.ids.services.abstractions.IReportService;
import it.unicam.cs.ids.utils.unitOfWork.IUnitOfWork;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing reports/violation flagging.
 * <p>
 * Allows mentors to report any misconduct
 * or rule violations by teams. Also provides
 * organizers with methods to review such reports and take
 * disciplinary actions.
 * </p>
 */
@Service
@Transactional
public class ReportService implements IReportService {

    private final IUnitOfWork unitOfWork;

    public ReportService(IUnitOfWork unitOfWork) {

        this.unitOfWork = unitOfWork;
    }

    @Override
    public ReportResponseDTO createReport(CreateReportDTO request, Long mentorId) {

        // EXTRACT THE ID DIRECTLY FROM THE JWT TOKEN!

        // 2. Retrieve entities (already safe thanks to the validator)
        StaffUser mentor = unitOfWork.getStaffUserRepository().findById(mentorId).orElse(null);
        Team team = unitOfWork.getTeamRepository().findById(request.teamId()).orElse(null);
        Hackathon hackathon = unitOfWork.getHackathonRepository().findById(request.hackathonId()).orElse(null);

        if (hackathon == null) {
            throw new ResourceNotFoundException("Hackathon not found");
        }

        // Check whether the mentor is assigned to this hackathon
        boolean isMentorAssigned = hackathon.getMentors().stream()
                .anyMatch(m -> m.getId().equals(mentorId));
        if (!isMentorAssigned) {
            throw new UnauthorizedActionException("The mentor is not assigned to this hackathon and cannot submit reports.");
        }

        // 3. Create the entity
        Report report = new Report();
        report.setMentor(mentor);
        report.setTeam(team);
        report.setHackathon(hackathon);
        report.setDescription(request.description());
        report.setStatus(ReportStatus.PENDING);
        report.setCreatedAt(LocalDateTime.now());
        report.setDecisionNote("N/D");

        // 4. Save
        unitOfWork.getReportRepository().save(report);

        //update the bidirectional relationship in memory
        hackathon.getReports().add(report); //should we write an add method on hackathon?

        // 5. Return DTO
        return mapToDTO(report);
    }

    @Override
    public List<ReportResponseDTO> getReportsForHackathon(Long hackathonId, Long organizerId) {

        Hackathon hackathon = unitOfWork.getHackathonRepository().findById(hackathonId).orElse(null);
        if (hackathon == null) throw new ResourceNotFoundException("Hackathon not found");

        // Only the organizer of the hackathon can view the reports
        if (!hackathon.getOrganizer().getId().equals(organizerId)) {
            throw new UnauthorizedActionException("Only the organizer can view the reports of this hackathon.");
        }

        List<Report> reports = unitOfWork.getReportRepository().findByHackathonId(hackathonId);

        return reports.stream()
                .map(this::mapToDTO).
                collect(Collectors.toList());
    }

    @Override
    public ReportResponseDTO respondToReport(UpdateReportDTO request, Long organizerId) {

        Report report = unitOfWork.getReportRepository().findById(request.reportId()).orElse(null);
        if (report == null) throw new ResourceNotFoundException("Report not found");

        if (!report.getHackathon().getOrganizer().getId().equals(organizerId)) {
            throw new UnauthorizedActionException("Only the organizer can update the status of this report.");
        }

        if (request.decisionNote() == null || request.decisionNote().isEmpty()) {
            throw new InvalidInputException("A decision note must be specified before updating the report status.");
        }

        if (report.getStatus() != ReportStatus.PENDING) {
            throw new RuleViolationException("The report has already been handled");
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