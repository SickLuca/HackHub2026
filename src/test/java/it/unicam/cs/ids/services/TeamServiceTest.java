package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.CreateTeamDTO;
import it.unicam.cs.ids.dtos.SubscribeTeamDTO;
import it.unicam.cs.ids.dtos.TeamResponseDTO;
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

    private CreateTeamDTO validRequest;
    private DefaultUser mockCreator;

    @BeforeEach
    void setUp() {
        lenient().when(unitOfWork.getTeamRepository()).thenReturn(teamRepository);
        lenient().when(unitOfWork.getDefaultUserRepository()).thenReturn(defaultUserRepository);
        lenient().when(unitOfWork.getHackathonRepository()).thenReturn(hackathonRepository);

        validRequest = new CreateTeamDTO(1L, "Alpha Team");

        mockCreator = new DefaultUser();
        mockCreator.setId(1L);
        mockCreator.setName("Mario"); // Necessario per il DTO
        mockCreator.setSurname("Rossi");
        mockCreator.setRole(UserRole.USER_NO_TEAM);
        mockCreator.setTeam(null);
    }

    @Test
    void createTeam_Success() {
        doNothing().when(teamValidator).validate(validRequest);
        when(defaultUserRepository.getById(1L)).thenReturn(mockCreator);

        TeamResponseDTO result = teamService.createTeam(validRequest);

        assertNotNull(result);
        assertEquals("Alpha Team", result.name());
        assertEquals(1, result.membersName().size());
        assertEquals("Mario Rossi", result.membersName().get(0));

        assertEquals(UserRole.TEAM_LEADER, mockCreator.getRole());
        assertNotNull(mockCreator.getTeam());

        verify(teamValidator, times(1)).validate(validRequest);
        verify(defaultUserRepository, times(1)).getById(1L);
        verify(teamRepository, times(1)).create(any(Team.class));
        verify(defaultUserRepository, times(1)).update(mockCreator);
    }

    @Test
    void createTeam_ValidationFails_ThrowsException() {
        doThrow(new IllegalArgumentException("Il nome del team deve contenere almeno 5 caratteri."))
                .when(teamValidator).validate(validRequest);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            teamService.createTeam(validRequest);
        });

        assertEquals("Il nome del team deve contenere almeno 5 caratteri.", exception.getMessage());
        verify(defaultUserRepository, never()).getById(anyLong());
        verify(teamRepository, never()).create(any());
    }

    @Test
    void createTeam_UserNotFound_ThrowsException() {
        doNothing().when(teamValidator).validate(validRequest);
        when(defaultUserRepository.getById(1L)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            teamService.createTeam(validRequest);
        });

        assertEquals("User not found in the system", exception.getMessage());
        verify(teamRepository, never()).create(any());
    }

    @Test
    void createTeam_UserAlreadyInTeam_ThrowsException() {
        doNothing().when(teamValidator).validate(validRequest);

        Team existingTeam = new Team();
        existingTeam.setId(99L);
        mockCreator.setTeam(existingTeam);

        when(defaultUserRepository.getById(1L)).thenReturn(mockCreator);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            teamService.createTeam(validRequest);
        });

        assertEquals("User already belongs to a team!", exception.getMessage());
        verify(teamRepository, never()).create(any());
        verify(defaultUserRepository, never()).update(any());
    }

    @Test
    void subscribeToHackathon_Success() {
        SubscribeTeamDTO request = new SubscribeTeamDTO(10L, 100L, 1L);

        Team mockTeam = new Team();
        mockTeam.setId(10L);
        mockTeam.setName("Alpha Team");
        mockTeam.setMembers(new ArrayList<>(java.util.List.of(mockCreator)));
        mockTeam.setSubscribedHackathon(null);

        Hackathon mockHackathon = new Hackathon();
        mockHackathon.setId(100L);
        mockHackathon.setName("Hack 2026");
        mockHackathon.setStatus(HackathonStatus.REGISTRATION);
        mockHackathon.setMaxDimensionOfTeam(5);
        mockHackathon.setTeams(new ArrayList<>());

        mockCreator.setRole(UserRole.TEAM_LEADER);

        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);
        when(defaultUserRepository.getById(1L)).thenReturn(mockCreator);

        TeamResponseDTO result = teamService.subscribeToHackathon(request);

        assertNotNull(result);
        assertEquals("Hack 2026", result.subscribedHackathonName());

        verify(teamRepository, times(1)).getById(10L);
        verify(hackathonRepository, times(1)).getById(100L);
        verify(defaultUserRepository, times(1)).getById(1L);
        verify(teamRepository, times(1)).update(mockTeam);
    }

    @Test
    void subscribeToHackathon_HackathonNotInRegistration_ThrowsException() {
        SubscribeTeamDTO request = new SubscribeTeamDTO(10L, 100L, 1L);
        Team mockTeam = new Team();
        Hackathon mockHackathon = new Hackathon();
        mockHackathon.setStatus(HackathonStatus.IN_PROGRESS);

        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            teamService.subscribeToHackathon(request);
        });

        assertEquals("Hackathon is not in registration phase", exception.getMessage());
        verify(teamRepository, never()).update(any());
    }

    @Test
    void subscribeToHackathon_TeamAlreadySubscribed_ThrowsException() {
        SubscribeTeamDTO request = new SubscribeTeamDTO(10L, 100L, 1L);
        Team mockTeam = new Team();
        mockTeam.setSubscribedHackathon(new Hackathon());
        Hackathon mockHackathon = new Hackathon();
        mockHackathon.setStatus(HackathonStatus.REGISTRATION);

        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            teamService.subscribeToHackathon(request);
        });

        assertEquals("Team is already subscribed to an Hackathon.", exception.getMessage());
        verify(teamRepository, never()).update(any());
    }

    @Test
    void subscribeToHackathon_TeamTooBig_ThrowsException() {
        SubscribeTeamDTO request = new SubscribeTeamDTO(10L, 100L, 1L);
        Team mockTeam = new Team();
        mockTeam.setMembers(new ArrayList<>(java.util.List.of(new DefaultUser(), new DefaultUser(), new DefaultUser())));

        Hackathon mockHackathon = new Hackathon();
        mockHackathon.setStatus(HackathonStatus.REGISTRATION);
        mockHackathon.setMaxDimensionOfTeam(2);

        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            teamService.subscribeToHackathon(request);
        });

        assertEquals("Team is too big for the hackathon", exception.getMessage());
        verify(teamRepository, never()).update(any());
    }

    @Test
    void subscribeToHackathon_UserNotLeader_ThrowsException() {
        SubscribeTeamDTO request = new SubscribeTeamDTO(10L, 100L, 1L);
        Team mockTeam = new Team();
        mockTeam.setMembers(new ArrayList<>(java.util.List.of(mockCreator)));

        Hackathon mockHackathon = new Hackathon();
        mockHackathon.setStatus(HackathonStatus.REGISTRATION);
        mockHackathon.setMaxDimensionOfTeam(5);

        mockCreator.setRole(UserRole.TEAM_MEMBER);

        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);
        when(defaultUserRepository.getById(1L)).thenReturn(mockCreator);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            teamService.subscribeToHackathon(request);
        });

        assertEquals("User is not a team leader", exception.getMessage());
        verify(teamRepository, never()).update(any());
    }
}