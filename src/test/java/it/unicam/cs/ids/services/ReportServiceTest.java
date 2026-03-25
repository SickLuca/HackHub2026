package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.requests.CreateReportDTO;
import it.unicam.cs.ids.dtos.requests.UpdateReportDTO;
import it.unicam.cs.ids.dtos.responses.ReportResponseDTO;
import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.models.Report;
import it.unicam.cs.ids.models.StaffUser;
import it.unicam.cs.ids.models.Team;
import it.unicam.cs.ids.models.utils.ReportStatus;
import it.unicam.cs.ids.repositories.abstractions.IHackathonRepository;
import it.unicam.cs.ids.repositories.abstractions.IReportRepository;
import it.unicam.cs.ids.repositories.abstractions.IStaffUserRepository;
import it.unicam.cs.ids.repositories.abstractions.ITeamRepository;
import it.unicam.cs.ids.utils.unitOfWork.IUnitOfWork;
import it.unicam.cs.ids.validators.abstractions.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private IUnitOfWork unitOfWork;

    @Mock
    private IStaffUserRepository staffUserRepository;

    @Mock
    private ITeamRepository teamRepository;

    @Mock
    private IHackathonRepository hackathonRepository;

    @Mock
    private IReportRepository reportRepository;

    @Mock
    private Validator<CreateReportDTO> validator;

    private ReportService reportService;

    // DTO e Mock standard da usare nei test
    private CreateReportDTO validCreateRequest;
    private UpdateReportDTO validUpdateRequest;
    private StaffUser mockMentor;
    private StaffUser mockOrganizer;
    private Team mockTeam;
    private Hackathon mockHackathon;
    private Report mockReport;

    @BeforeEach
    void setUp() {
        lenient().when(unitOfWork.getStaffUserRepository()).thenReturn(staffUserRepository);
        lenient().when(unitOfWork.getTeamRepository()).thenReturn(teamRepository);
        lenient().when(unitOfWork.getHackathonRepository()).thenReturn(hackathonRepository);
        lenient().when(unitOfWork.getReportRepository()).thenReturn(reportRepository);

        // Iniezione manuale e sicura
        reportService = new ReportService(unitOfWork, validator);

        // --- Setup Dati Standard ---
        validCreateRequest = new CreateReportDTO(50L, 10L, 100L, "Il team ha copiato codice da un'altra repository.");
        validUpdateRequest = new UpdateReportDTO(1L, 20L, "Squalifica immediata dal torneo.");

        mockMentor = new StaffUser();
        mockMentor.setId(50L);
        mockMentor.setName("Marco");
        mockMentor.setSurname("Rossi");

        mockOrganizer = new StaffUser();
        mockOrganizer.setId(20L);
        mockOrganizer.setName("Giulia");
        mockOrganizer.setSurname("Bianchi");

        mockHackathon = new Hackathon();
        mockHackathon.setId(100L);
        mockHackathon.setName("HackHub CodeFest");
        mockHackathon.setOrganizer(mockOrganizer);

        mockTeam = new Team();
        mockTeam.setId(10L);
        mockTeam.setName("Team BugMakers");

        mockReport = new Report();
        mockReport.setId(1L);
        mockReport.setMentor(mockMentor);
        mockReport.setTeam(mockTeam);
        mockReport.setHackathon(mockHackathon);
        mockReport.setDescription("Comportamento scorretto");
        mockReport.setStatus(ReportStatus.PENDING);
        mockReport.setDecisionNote("N/D");
        mockReport.setCreatedAt(LocalDateTime.now());
    }

    // TEST METODO: createReport

    @Test
    void createReport_Success() {
        when(staffUserRepository.getById(50L)).thenReturn(mockMentor);
        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);
        when(reportRepository.create(any(Report.class))).thenAnswer(i -> i.getArgument(0));

        ReportResponseDTO result = reportService.createReport(validCreateRequest);

        assertNotNull(result);
        assertEquals(ReportStatus.PENDING, result.status());
        assertEquals("N/D", result.decisionNote());
        assertEquals("Marco Rossi", result.mentorFullName());

        verify(reportRepository, times(1)).create(any(Report.class));
    }

    // TEST METODO: getReportsForHackathon

    @Test
    void getReportsForHackathon_Success() {
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);
        when(reportRepository.getReportsByHackathonId(100L)).thenReturn(List.of(mockReport));

        // Facciamo la richiesta con l'ID dell'organizzatore (20L)
        List<ReportResponseDTO> results = reportService.getReportsForHackathon(100L, 20L);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Team BugMakers", results.get(0).teamName());
    }

    @Test
    void getReportsForHackathon_HackathonNotFound_ThrowsException() {
        when(hackathonRepository.getById(100L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> reportService.getReportsForHackathon(100L, 20L));
    }

    @Test
    void getReportsForHackathon_UserNotOrganizer_ThrowsException() {
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);

        // Facciamo la richiesta con l'ID di un utente che NON è l'organizzatore (es. 99L)
        assertThrows(SecurityException.class, () -> reportService.getReportsForHackathon(100L, 99L));
    }

    // TEST METODO: respondToReport

    @Test
    void respondToReport_Success() {
        when(reportRepository.getById(1L)).thenReturn(mockReport);
        when(reportRepository.update(any(Report.class))).thenAnswer(i -> i.getArgument(0));

        ReportResponseDTO result = reportService.respondToReport(validUpdateRequest);

        assertNotNull(result);
        assertEquals(ReportStatus.RESOLVED, result.status());
        assertEquals("Squalifica immediata dal torneo.", result.decisionNote());

        verify(reportRepository, times(1)).update(mockReport);
    }

    @Test
    void respondToReport_ReportNotFound_ThrowsException() {
        when(reportRepository.getById(1L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> reportService.respondToReport(validUpdateRequest));
    }

    @Test
    void respondToReport_UserNotOrganizer_ThrowsException() {
        UpdateReportDTO unauthorizedRequest = new UpdateReportDTO(1L, 99L, "Squalificati"); // 99L non è l'organizzatore

        when(reportRepository.getById(1L)).thenReturn(mockReport);

        assertThrows(SecurityException.class, () -> reportService.respondToReport(unauthorizedRequest));
    }

    @Test
    void respondToReport_ReportAlreadyResolved_ThrowsException() {
        mockReport.setStatus(ReportStatus.RESOLVED); // Segnalazione non più pendente

        when(reportRepository.getById(1L)).thenReturn(mockReport);

        assertThrows(IllegalStateException.class, () -> reportService.respondToReport(validUpdateRequest));
    }

    @Test
    void respondToReport_EmptyDecisionNote_ThrowsException() {
        // Test per stringa vuota
        UpdateReportDTO emptyRequest = new UpdateReportDTO(1L, 20L, "");

        when(reportRepository.getById(1L)).thenReturn(mockReport);

        assertThrows(IllegalArgumentException.class, () -> reportService.respondToReport(emptyRequest));
    }

    @Test
    void respondToReport_NullDecisionNote_ThrowsException() {
        // Test per stringa nulla
        UpdateReportDTO nullRequest = new UpdateReportDTO(1L, 20L, null);

        when(reportRepository.getById(1L)).thenReturn(mockReport);

        assertThrows(IllegalArgumentException.class, () -> reportService.respondToReport(nullRequest));
    }
}