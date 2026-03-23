package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.CreateSubmissionDTO;
import it.unicam.cs.ids.dtos.SubmissionResponseDTO;
import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.models.Submission;
import it.unicam.cs.ids.models.Team;
import it.unicam.cs.ids.repositories.abstractions.IHackathonRepository;
import it.unicam.cs.ids.repositories.abstractions.ISubmissionRepository;
import it.unicam.cs.ids.repositories.abstractions.ITeamRepository;
import it.unicam.cs.ids.utils.unitOfWork.IUnitOfWork;
import it.unicam.cs.ids.validators.abstractions.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

    @InjectMocks
    private SubmissionService submissionService;

    private CreateSubmissionDTO validRequest;
    private Team mockTeam;
    private Hackathon mockHackathon;

    @BeforeEach
    void setUp() {
        lenient().when(unitOfWork.getTeamRepository()).thenReturn(teamRepository);
        lenient().when(unitOfWork.getHackathonRepository()).thenReturn(hackathonRepository);
        lenient().when(unitOfWork.getSubmissionRepository()).thenReturn(submissionRepository);

        validRequest = new CreateSubmissionDTO(
                10L, 100L,
                "https://github.com/team/project",
                "Descrizione del progetto",
                LocalDateTime.now()
        );

        mockHackathon = new Hackathon();
        mockHackathon.setId(100L);
        mockHackathon.setName("Hack 2026"); // Serve per il DTO
        mockHackathon.setSubmitDeadline(LocalDateTime.now().plusDays(5));
        mockHackathon.setSubmissions(new ArrayList<>());

        mockTeam = new Team();
        mockTeam.setId(10L);
        mockTeam.setName("Team Alpha"); // Serve per il DTO
        mockTeam.setSubscribedHackathon(mockHackathon);
        mockTeam.setSubmissions(new ArrayList<>());
    }

    @Test
    void addSubmission_Success() {
        doNothing().when(submissionValidator).validate(validRequest);
        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);

        when(submissionRepository.create(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SubmissionResponseDTO result = submissionService.addSubmission(validRequest);

        assertNotNull(result);
        assertEquals("https://github.com/team/project", result.projectUrl());
        assertEquals(mockTeam.getName(), result.teamName());
        assertEquals(mockHackathon.getName(), result.hackathonName());

        assertEquals(1, mockTeam.getSubmissions().size());
        verify(submissionRepository, atLeastOnce()).create(any(Submission.class));
    }

    @Test
    void addSubmission_TeamNotSubscribedToHackathon_ThrowsException() {
        doNothing().when(submissionValidator).validate(validRequest);

        Hackathon anotherHackathon = new Hackathon();
        anotherHackathon.setId(999L);
        mockTeam.setSubscribedHackathon(anotherHackathon);

        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            submissionService.addSubmission(validRequest);
        });

        assertEquals("The team is not subscribed to this Hackathon.", exception.getMessage());
        verify(submissionRepository, never()).create(any());
    }

    @Test
    void addSubmission_DeadlinePassed_ThrowsException() {
        doNothing().when(submissionValidator).validate(validRequest);
        mockHackathon.setSubmitDeadline(LocalDateTime.now().minusDays(1));

        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            submissionService.addSubmission(validRequest);
        });

        assertEquals("The submission deadline has already passed.", exception.getMessage());
        verify(submissionRepository, never()).create(any());
    }

    @Test
    void addSubmission_SubmissionAlreadyExists_ThrowsException() {
        doNothing().when(submissionValidator).validate(validRequest);

        Submission existingSubmission = new Submission();
        existingSubmission.setHackathon(mockHackathon);
        mockTeam.getSubmissions().add(existingSubmission);

        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            submissionService.addSubmission(validRequest);
        });

        assertEquals("A submission for this Hackathon already exists.", exception.getMessage());
        verify(submissionRepository, never()).create(any());
    }
}