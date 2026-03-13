package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.CreateTeamDTO;
import it.unicam.cs.ids.dtos.SubscribeTeamDTO;
import it.unicam.cs.ids.models.DefaultUser;
import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.models.Team;
import it.unicam.cs.ids.models.utils.HackathonStatus;
import it.unicam.cs.ids.models.utils.UserRole;
import it.unicam.cs.ids.repositories.abstractions.IDefaultUserRepository;
import it.unicam.cs.ids.repositories.abstractions.IHackathonRepository;
import it.unicam.cs.ids.repositories.abstractions.ITeamRepository;
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
    private ITeamRepository teamRepository;

    @Mock
    private IDefaultUserRepository defaultUserRepository;

    @Mock
    private IHackathonRepository hackathonRepository; // Iniettato anche se non usato nella creazione

    @Mock
    private Validator<CreateTeamDTO> teamValidator;

    @InjectMocks
    private TeamService teamService;

    private CreateTeamDTO validRequest;
    private DefaultUser mockCreator;

    @BeforeEach
    void setUp() {
        validRequest = new CreateTeamDTO(1L, "Alpha Team");

        mockCreator = new DefaultUser();
        mockCreator.setId(1L);
        mockCreator.setRole(UserRole.USER_NO_TEAM); // Ruolo iniziale
        mockCreator.setTeam(null); // Nessun team iniziale
    }

    @Test
    void createTeam_Success() {
        // Arrange
        doNothing().when(teamValidator).validate(validRequest);
        when(defaultUserRepository.getById(1L)).thenReturn(mockCreator);

        Team expectedSavedTeam = new Team();
        expectedSavedTeam.setId(10L);
        expectedSavedTeam.setName("Alpha Team");
        expectedSavedTeam.setMembers(new ArrayList<>(java.util.List.of(mockCreator)));

        when(teamRepository.create(any(Team.class))).thenReturn(expectedSavedTeam);
        when(defaultUserRepository.update(any(DefaultUser.class))).thenReturn(mockCreator);

        // Act
        Team result = teamService.createTeam(validRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Alpha Team", result.getName());
        assertEquals(1, result.getMembers().size());

        // Verifichiamo che l'utente sia stato promosso a TEAM_LEADER
        assertEquals(UserRole.TEAM_LEADER, mockCreator.getRole());
        assertNotNull(mockCreator.getTeam());

        // Verifichiamo le interazioni con i mock
        verify(teamValidator, times(1)).validate(validRequest);
        verify(defaultUserRepository, times(1)).getById(1L);
        verify(teamRepository, times(1)).create(any(Team.class));
        verify(defaultUserRepository, times(1)).update(mockCreator);
    }

    @Test
    void createTeam_ValidationFails_ThrowsException() {
        // Arrange
        doThrow(new IllegalArgumentException("Il nome del team deve contenere almeno 5 caratteri."))
                .when(teamValidator).validate(validRequest);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            teamService.createTeam(validRequest);
        });

        assertEquals("Il nome del team deve contenere almeno 5 caratteri.", exception.getMessage());

        // Verifichiamo che il DB non sia stato toccato
        verify(defaultUserRepository, never()).getById(anyLong());
        verify(teamRepository, never()).create(any());
    }

    @Test
    void createTeam_UserNotFound_ThrowsException() {
        // Arrange
        doNothing().when(teamValidator).validate(validRequest);
        when(defaultUserRepository.getById(1L)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            teamService.createTeam(validRequest);
        });

        assertEquals("User not found in the system", exception.getMessage());
        verify(teamRepository, never()).create(any());
    }

    @Test
    void createTeam_UserAlreadyInTeam_ThrowsException() {
        // Arrange
        doNothing().when(teamValidator).validate(validRequest);

        // Assegniamo già un team fittizio all'utente
        Team existingTeam = new Team();
        existingTeam.setId(99L);
        mockCreator.setTeam(existingTeam);

        when(defaultUserRepository.getById(1L)).thenReturn(mockCreator);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            teamService.createTeam(validRequest);
        });

        assertEquals("User already belongs to a team!", exception.getMessage());
        verify(teamRepository, never()).create(any());
        verify(defaultUserRepository, never()).update(any());
    }

    @Test
    void subscribeToHackathon_Success() {
        // Arrange
        SubscribeTeamDTO request = new SubscribeTeamDTO(10L, 100L, 1L);

        Team mockTeam = new Team();
        mockTeam.setId(10L);
        mockTeam.setMembers(new ArrayList<>(java.util.List.of(mockCreator))); // Dimensione 1
        mockTeam.setSubscribedHackathon(null); // Non ancora iscritto

        Hackathon mockHackathon = new Hackathon();
        mockHackathon.setId(100L);
        mockHackathon.setStatus(HackathonStatus.REGISTRATION);
        mockHackathon.setMaxDimensionOfTeam(5);

        mockCreator.setRole(UserRole.TEAM_LEADER);

        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);
        when(defaultUserRepository.getById(1L)).thenReturn(mockCreator);
        when(teamRepository.update(any(Team.class))).thenReturn(mockTeam);

        // Act
        Team result = teamService.subscribeToHackathon(request);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getSubscribedHackathon().getId());

        verify(teamRepository, times(1)).getById(10L);
        verify(hackathonRepository, times(1)).getById(100L);
        verify(defaultUserRepository, times(1)).getById(1L);
        verify(teamRepository, times(1)).update(mockTeam);
    }

    @Test
    void subscribeToHackathon_HackathonNotInRegistration_ThrowsException() {
        // Arrange
        SubscribeTeamDTO request = new SubscribeTeamDTO(10L, 100L, 1L);
        Team mockTeam = new Team();
        Hackathon mockHackathon = new Hackathon();
        mockHackathon.setStatus(HackathonStatus.IN_PROGRESS); // Stato errato

        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            teamService.subscribeToHackathon(request);
        });

        assertEquals("Hackathon is not in registration phase", exception.getMessage());
        verify(teamRepository, never()).update(any());
    }

    @Test
    void subscribeToHackathon_TeamAlreadySubscribed_ThrowsException() {
        // Arrange
        SubscribeTeamDTO request = new SubscribeTeamDTO(10L, 100L, 1L);
        Team mockTeam = new Team();
        mockTeam.setSubscribedHackathon(new Hackathon()); // Già iscritto a un hackathon
        Hackathon mockHackathon = new Hackathon();
        mockHackathon.setStatus(HackathonStatus.REGISTRATION);

        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            teamService.subscribeToHackathon(request);
        });

        assertEquals("Team is already subscribed to an Hackathon.", exception.getMessage());
        verify(teamRepository, never()).update(any());
    }

    @Test
    void subscribeToHackathon_TeamTooBig_ThrowsException() {
        // Arrange
        SubscribeTeamDTO request = new SubscribeTeamDTO(10L, 100L, 1L);
        Team mockTeam = new Team();
        // Simuliamo un team di 3 persone
        mockTeam.setMembers(new ArrayList<>(java.util.List.of(new DefaultUser(), new DefaultUser(), new DefaultUser())));

        Hackathon mockHackathon = new Hackathon();
        mockHackathon.setStatus(HackathonStatus.REGISTRATION);
        mockHackathon.setMaxDimensionOfTeam(2); // Limite di 2 persone

        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            teamService.subscribeToHackathon(request);
        });

        assertEquals("Team is too big for the hackathon", exception.getMessage());
        verify(teamRepository, never()).update(any());
    }

    @Test
    void subscribeToHackathon_UserNotLeader_ThrowsException() {
        // Arrange
        SubscribeTeamDTO request = new SubscribeTeamDTO(10L, 100L, 1L);
        Team mockTeam = new Team();
        mockTeam.setMembers(new ArrayList<>(java.util.List.of(mockCreator)));

        Hackathon mockHackathon = new Hackathon();
        mockHackathon.setStatus(HackathonStatus.REGISTRATION);
        mockHackathon.setMaxDimensionOfTeam(5);

        mockCreator.setRole(UserRole.TEAM_MEMBER); // Ruolo errato

        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);
        when(defaultUserRepository.getById(1L)).thenReturn(mockCreator);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            teamService.subscribeToHackathon(request);
        });

        assertEquals("User is not a team leader", exception.getMessage());
        verify(teamRepository, never()).update(any());
    }
}