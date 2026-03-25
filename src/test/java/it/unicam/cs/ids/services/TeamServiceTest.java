package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.requests.CreateTeamDTO;
import it.unicam.cs.ids.dtos.requests.SubscribeTeamDTO;
import it.unicam.cs.ids.dtos.responses.TeamResponseDTO;
import it.unicam.cs.ids.models.DefaultUser;
import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.models.Team;
import it.unicam.cs.ids.models.utils.HackathonStatus;
import it.unicam.cs.ids.models.utils.UserRole;
import it.unicam.cs.ids.repositories.abstractions.IDefaultUserRepository;
import it.unicam.cs.ids.repositories.abstractions.IHackathonRepository;
import it.unicam.cs.ids.repositories.abstractions.ITeamRepository;
import it.unicam.cs.ids.utils.unitOfWork.IUnitOfWork;
import it.unicam.cs.ids.validators.abstractions.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private IUnitOfWork unitOfWork;

    @Mock
    private ITeamRepository teamRepository;

    @Mock
    private IDefaultUserRepository defaultUserRepository;

    @Mock
    private IHackathonRepository hackathonRepository;

    @Mock
    private Validator<CreateTeamDTO> teamValidator;

    @InjectMocks
    private TeamService teamService;

    private CreateTeamDTO validCreateRequest;
    private SubscribeTeamDTO validSubscribeRequest;
    private DefaultUser mockCreator;
    private Team mockTeam;
    private Hackathon mockHackathon;

    @BeforeEach
    void setUp() {
        lenient().when(unitOfWork.getTeamRepository()).thenReturn(teamRepository);
        lenient().when(unitOfWork.getDefaultUserRepository()).thenReturn(defaultUserRepository);
        lenient().when(unitOfWork.getHackathonRepository()).thenReturn(hackathonRepository);

        validCreateRequest = new CreateTeamDTO(1L, "Alpha Team");
        validSubscribeRequest = new SubscribeTeamDTO(10L, 100L, 1L);

        mockCreator = new DefaultUser();
        mockCreator.setId(1L);
        mockCreator.setName("Mario");
        mockCreator.setSurname("Rossi");
        mockCreator.setRole(UserRole.USER_NO_TEAM);

        mockTeam = new Team();
        mockTeam.setId(10L);
        mockTeam.setName("Alpha Team");
        mockTeam.setMembers(new ArrayList<>(java.util.List.of(mockCreator)));

        mockHackathon = new Hackathon();
        mockHackathon.setId(100L);
        mockHackathon.setName("Hack 2026");
        mockHackathon.setStatus(HackathonStatus.REGISTRATION);
        mockHackathon.setMaxDimensionOfTeam(5);
        mockHackathon.setTeams(new ArrayList<>());
    }

    // --- TEST METODO createTeam ---

    @Test
    void createTeam_Success() {
        doNothing().when(teamValidator).validate(validCreateRequest);
        when(defaultUserRepository.getById(1L)).thenReturn(mockCreator);

        TeamResponseDTO result = teamService.createTeam(validCreateRequest);

        assertNotNull(result);
        assertEquals("Alpha Team", result.name());
        verify(teamRepository, times(1)).create(any(Team.class));
        verify(defaultUserRepository, times(1)).update(mockCreator);
    }

    @Test
    void createTeam_ValidationFails_ThrowsException() {
        doThrow(new IllegalArgumentException("Nome troppo corto")).when(teamValidator).validate(validCreateRequest);
        assertThrows(IllegalArgumentException.class, () -> teamService.createTeam(validCreateRequest));
        verify(teamRepository, never()).create(any());
    }

    @Test
    void createTeam_UserNotFound_ThrowsException() {
        doNothing().when(teamValidator).validate(validCreateRequest);
        when(defaultUserRepository.getById(1L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> teamService.createTeam(validCreateRequest));
    }

    @Test
    void createTeam_UserAlreadyInTeam_ThrowsException() {
        doNothing().when(teamValidator).validate(validCreateRequest);
        mockCreator.setTeam(new Team());
        when(defaultUserRepository.getById(1L)).thenReturn(mockCreator);
        assertThrows(IllegalStateException.class, () -> teamService.createTeam(validCreateRequest));
    }

    // --- TEST METODO subscribeToHackathon ---

    @Test
    void subscribeToHackathon_Success() {
        mockCreator.setRole(UserRole.TEAM_LEADER);
        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);
        when(defaultUserRepository.getById(1L)).thenReturn(mockCreator);

        TeamResponseDTO result = teamService.subscribeToHackathon(validSubscribeRequest);

        assertNotNull(result);
        assertEquals("Hack 2026", result.subscribedHackathonName());
        verify(teamRepository, times(1)).update(mockTeam);
    }

    @Test
    void subscribeToHackathon_TeamNotFound_ThrowsException() {
        when(teamRepository.getById(10L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> teamService.subscribeToHackathon(validSubscribeRequest));
    }

    @Test
    void subscribeToHackathon_HackathonNotFound_ThrowsException() {
        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(hackathonRepository.getById(100L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> teamService.subscribeToHackathon(validSubscribeRequest));
    }

    @Test
    void subscribeToHackathon_UserNotFound_ThrowsException() {
        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);

        when(defaultUserRepository.getById(1L)).thenReturn(null);

        assertThrows(Exception.class, () -> teamService.subscribeToHackathon(validSubscribeRequest));
    }

    @Test
    void subscribeToHackathon_HackathonNotInRegistration_ThrowsException() {
        mockHackathon.setStatus(HackathonStatus.IN_PROGRESS);
        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);
        assertThrows(IllegalStateException.class, () -> teamService.subscribeToHackathon(validSubscribeRequest));
    }

    @Test
    void subscribeToHackathon_TeamAlreadySubscribed_ThrowsException() {
        mockTeam.setSubscribedHackathon(new Hackathon());
        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);
        assertThrows(IllegalStateException.class, () -> teamService.subscribeToHackathon(validSubscribeRequest));
    }

    @Test
    void subscribeToHackathon_TeamTooBig_ThrowsException() {
        mockTeam.setMembers(new ArrayList<>(java.util.List.of(new DefaultUser(), new DefaultUser(), new DefaultUser())));
        mockHackathon.setMaxDimensionOfTeam(2);
        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);
        assertThrows(IllegalStateException.class, () -> teamService.subscribeToHackathon(validSubscribeRequest));
    }

    @Test
    void subscribeToHackathon_UserNotLeader_ThrowsException() {
        mockCreator.setRole(UserRole.TEAM_MEMBER);
        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);
        when(defaultUserRepository.getById(1L)).thenReturn(mockCreator);
        assertThrows(IllegalStateException.class, () -> teamService.subscribeToHackathon(validSubscribeRequest));
    }
}