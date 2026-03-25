package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.requests.CreateSubmissionDTO;
import it.unicam.cs.ids.dtos.requests.EvaluateSubmissionDTO;
import it.unicam.cs.ids.dtos.requests.UpdateSubmissionDTO;
import it.unicam.cs.ids.dtos.responses.SubmissionResponseDTO;
import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.models.StaffUser;
import it.unicam.cs.ids.models.Submission;
import it.unicam.cs.ids.models.Team;
import it.unicam.cs.ids.models.utils.HackathonStatus;
import it.unicam.cs.ids.models.utils.SubmissionStatus;
import it.unicam.cs.ids.repositories.abstractions.IHackathonRepository;
import it.unicam.cs.ids.repositories.abstractions.ISubmissionRepository;
import it.unicam.cs.ids.repositories.abstractions.ITeamRepository;
import it.unicam.cs.ids.utils.unitOfWork.IUnitOfWork;
import it.unicam.cs.ids.validators.abstractions.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock
    private IUnitOfWork unitOfWork;

    @Mock
    private ITeamRepository teamRepository;

    @Mock
    private IHackathonRepository hackathonRepository;

    @Mock
    private ISubmissionRepository submissionRepository;

    @Mock
    private Validator<CreateSubmissionDTO> submissionValidator;

    @Mock
    private Validator<EvaluateSubmissionDTO> evaluateSubmissionValidator;

    //@InjectMocks
    private SubmissionService submissionService;

    private CreateSubmissionDTO validRequest;
    private Team mockTeam;
    private Hackathon mockHackathon;

    @BeforeEach
    void setUp() {
        lenient().when(unitOfWork.getTeamRepository()).thenReturn(teamRepository);
        lenient().when(unitOfWork.getHackathonRepository()).thenReturn(hackathonRepository);
        lenient().when(unitOfWork.getSubmissionRepository()).thenReturn(submissionRepository);

        submissionService = new SubmissionService(unitOfWork, submissionValidator, evaluateSubmissionValidator);

        validRequest = new CreateSubmissionDTO(
                10L, 100L,
                "https://github.com/team/project",
                "Descrizione del progetto",
                LocalDateTime.now()
        );

        mockHackathon = new Hackathon();
        mockHackathon.setId(100L);
        mockHackathon.setName("Hack 2026");
        mockHackathon.setSubmitDeadline(LocalDateTime.now().plusDays(5));
        mockHackathon.setSubmissions(new ArrayList<>());

        mockTeam = new Team();
        mockTeam.setId(10L);
        mockTeam.setName("Team Alpha");
        mockTeam.setSubscribedHackathon(mockHackathon);
        mockTeam.setSubmissions(new ArrayList<>());
    }

    @Test
    void addSubmission_Success() {
        doNothing().when(submissionValidator).validate(validRequest);
        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);
        when(submissionRepository.create(any(Submission.class))).thenAnswer(i -> i.getArgument(0));

        SubmissionResponseDTO result = submissionService.addSubmission(validRequest);

        assertNotNull(result);
        assertEquals("https://github.com/team/project", result.projectUrl());
        verify(submissionRepository, times(1)).create(any(Submission.class));
    }

    @Test
    void addSubmission_ValidatorFails_ThrowsException() {
        doThrow(new IllegalArgumentException("URL non valido")).when(submissionValidator).validate(validRequest);
        assertThrows(IllegalArgumentException.class, () -> submissionService.addSubmission(validRequest));
        verify(submissionRepository, never()).create(any());
    }

    @Test
    void addSubmission_TeamNotFound_ThrowsException() {
        //doNothing().when(submissionValidator).validate(validRequest);
        when(teamRepository.getById(10L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> submissionService.addSubmission(validRequest));
    }

    @Test
    void addSubmission_HackathonNotFound_ThrowsException() {
        doNothing().when(submissionValidator).validate(validRequest);
        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(hackathonRepository.getById(100L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> submissionService.addSubmission(validRequest));
    }

    @Test
    void addSubmission_TeamNotSubscribedToHackathon_ThrowsException() {
        doNothing().when(submissionValidator).validate(validRequest);
        Hackathon anotherHackathon = new Hackathon();
        anotherHackathon.setId(999L);
        mockTeam.setSubscribedHackathon(anotherHackathon);

        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);

        assertThrows(IllegalStateException.class, () -> submissionService.addSubmission(validRequest));
    }

    @Test
    void addSubmission_DeadlinePassed_ThrowsException() {
        doNothing().when(submissionValidator).validate(validRequest);
        mockHackathon.setSubmitDeadline(LocalDateTime.now().minusDays(1));

        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);

        assertThrows(IllegalStateException.class, () -> submissionService.addSubmission(validRequest));
    }

    @Test
    void addSubmission_SubmissionAlreadyExists_ThrowsException() {
        //doNothing().when(submissionValidator).validate(validRequest);
        Submission existingSubmission = new Submission();
        existingSubmission.setHackathon(mockHackathon);
        mockTeam.getSubmissions().add(existingSubmission);

        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);

        assertThrows(IllegalStateException.class, () -> submissionService.addSubmission(validRequest));
    }

    @Test
    void updateSubmission_Success() {
        UpdateSubmissionDTO request = new UpdateSubmissionDTO(1L, "https://github.com/new-url", "Nuova descrizione", LocalDateTime.now());
        Submission mockSubmission = new Submission();
        mockSubmission.setId(1L);
        mockSubmission.setHackathon(mockHackathon); // La deadline è nel futuro (impostata nel setUp)
        mockSubmission.setTeam(mockTeam);
        mockSubmission.setStatus(SubmissionStatus.OPEN);

        when(submissionRepository.getById(1L)).thenReturn(mockSubmission);
        when(submissionRepository.update(any(Submission.class))).thenAnswer(i -> i.getArgument(0));

        SubmissionResponseDTO result = submissionService.updateSubmission(request);

        assertNotNull(result);
        assertEquals("https://github.com/new-url", result.projectUrl());
        assertEquals("Nuova descrizione", result.description());
        verify(submissionRepository, times(1)).update(mockSubmission);
    }

    @Test
    void updateSubmission_SubmissionNotFound_ThrowsException() {
        UpdateSubmissionDTO request = new UpdateSubmissionDTO(1L, "url", "desc", LocalDateTime.now());
        when(submissionRepository.getById(1L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> submissionService.updateSubmission(request));
    }

    @Test
    void updateSubmission_DeadlinePassed_ThrowsException() {
        UpdateSubmissionDTO request = new UpdateSubmissionDTO(1L, "url", "desc", LocalDateTime.now());
        mockHackathon.setSubmitDeadline(LocalDateTime.now().minusDays(1)); // Scadenza passata

        Submission mockSubmission = new Submission();
        mockSubmission.setId(1L);
        mockSubmission.setHackathon(mockHackathon);

        when(submissionRepository.getById(1L)).thenReturn(mockSubmission);

        assertThrows(IllegalStateException.class, () -> submissionService.updateSubmission(request));
    }

    @Test
    void evaluateSubmission_Success() {
        EvaluateSubmissionDTO request = new EvaluateSubmissionDTO(1L, 50L, 8, "Ottimo lavoro");
        //doNothing().when(evaluateSubmissionValidator).validate(request);

        StaffUser mockJudge = new StaffUser();
        mockJudge.setId(50L);
        mockJudge.setName("Giudice");
        mockJudge.setSurname("Severo");

        mockHackathon.setJudge(mockJudge);
        mockHackathon.setStatus(HackathonStatus.IN_PROGRESS);

        Submission mockSubmission = new Submission();
        mockSubmission.setId(1L);
        mockSubmission.setHackathon(mockHackathon);
        mockSubmission.setTeam(mockTeam);
        mockSubmission.setStatus(SubmissionStatus.CLOSED);

        when(submissionRepository.getById(1L)).thenReturn(mockSubmission);
        when(submissionRepository.update(any(Submission.class))).thenAnswer(i -> i.getArgument(0));

        SubmissionResponseDTO result = submissionService.evaluateSubmission(request);

        assertNotNull(result);
        assertEquals(8, result.score());
        assertEquals("Ottimo lavoro", result.feedback());
        verify(submissionRepository, times(1)).update(mockSubmission);
    }

    @Test
    void evaluateSubmission_WrongJudge_ThrowsException() {
        EvaluateSubmissionDTO request = new EvaluateSubmissionDTO(1L, 99L, 8, "Ottimo"); // 99L non è il giudice
        doNothing().when(evaluateSubmissionValidator).validate(request);

        StaffUser realJudge = new StaffUser();
        realJudge.setId(50L);
        mockHackathon.setJudge(realJudge);

        Submission mockSubmission = new Submission();
        mockSubmission.setId(1L);
        mockSubmission.setHackathon(mockHackathon);

        when(submissionRepository.getById(1L)).thenReturn(mockSubmission);

        assertThrows(SecurityException.class, () -> submissionService.evaluateSubmission(request));
    }

    @Test
    void evaluateSubmission_HackathonNotInProgress_ThrowsException() {
        EvaluateSubmissionDTO request = new EvaluateSubmissionDTO(1L, 50L, 8, "Ottimo");
        //doNothing().when(evaluateSubmissionValidator).validate(request);

        StaffUser mockJudge = new StaffUser();
        mockJudge.setId(50L);
        mockHackathon.setJudge(mockJudge);
        mockHackathon.setStatus(HackathonStatus.REGISTRATION); // Sbagliato, deve essere IN_PROGRESS

        Submission mockSubmission = new Submission();
        mockSubmission.setId(1L);
        mockSubmission.setHackathon(mockHackathon);

        when(submissionRepository.getById(1L)).thenReturn(mockSubmission);

        assertThrows(IllegalStateException.class, () -> submissionService.evaluateSubmission(request));
    }

    @Test
    void evaluateSubmission_SubmissionNotClosed_ThrowsException() {
        EvaluateSubmissionDTO request = new EvaluateSubmissionDTO(1L, 50L, 8, "Ottimo");
        //doNothing().when(evaluateSubmissionValidator).validate(request);

        StaffUser mockJudge = new StaffUser();
        mockJudge.setId(50L);
        mockHackathon.setJudge(mockJudge);
        mockHackathon.setStatus(HackathonStatus.IN_PROGRESS);

        Submission mockSubmission = new Submission();
        mockSubmission.setId(1L);
        mockSubmission.setHackathon(mockHackathon);
        mockSubmission.setStatus(SubmissionStatus.OPEN); // Sbagliato, deve essere CLOSED

        when(submissionRepository.getById(1L)).thenReturn(mockSubmission);

        assertThrows(IllegalStateException.class, () -> submissionService.evaluateSubmission(request));
    }

}