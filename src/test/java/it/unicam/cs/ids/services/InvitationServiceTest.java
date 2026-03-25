package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.requests.CreateInvitationDTO;
import it.unicam.cs.ids.dtos.requests.RespondInvitationDTO;
import it.unicam.cs.ids.dtos.responses.InvitationResponseDTO;
import it.unicam.cs.ids.models.DefaultUser;
import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.models.Invitation;
import it.unicam.cs.ids.models.Team;
import it.unicam.cs.ids.models.utils.InvitationStatus;
import it.unicam.cs.ids.models.utils.UserRole;
import it.unicam.cs.ids.repositories.abstractions.IDefaultUserRepository;
import it.unicam.cs.ids.repositories.abstractions.IInvitationRepository;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

    @Mock
    private IUnitOfWork unitOfWork;

    @Mock
    private ITeamRepository teamRepository;

    @Mock
    private IDefaultUserRepository defaultUserRepository;

    @Mock
    private IInvitationRepository invitationRepository;

    @Mock
    private Validator<CreateInvitationDTO> validator;

    private InvitationService invitationService;

    // DTO e Mock standard da usare nei test
    private CreateInvitationDTO validCreateRequest;
    private Team mockTeam;
    private DefaultUser mockLeader;
    private DefaultUser mockInvitedUser;
    private Invitation mockInvitation;

    @BeforeEach
    void setUp() {
        // Colleghiamo i repository mockati alla UnitOfWork in modo "lenient" così Mockito non si lamenta se in un test specifico non li usiamo tutti.
        lenient().when(unitOfWork.getTeamRepository()).thenReturn(teamRepository);
        lenient().when(unitOfWork.getDefaultUserRepository()).thenReturn(defaultUserRepository);
        lenient().when(unitOfWork.getInvitationRepository()).thenReturn(invitationRepository);

        // Iniezione manuale sicura (no @InjectMocks)
        invitationService = new InvitationService(unitOfWork, validator);

        // --- Setup Dati Standard ---
        validCreateRequest = new CreateInvitationDTO(1L, "Unisciti a noi!", 10L, 2L, LocalDateTime.now());

        mockTeam = new Team();
        mockTeam.setId(10L);
        mockTeam.setName("Alpha Team");
        mockTeam.setMembers(new ArrayList<>());

        mockLeader = new DefaultUser(1L);
        mockLeader.setName("Mario");
        mockLeader.setRole(UserRole.TEAM_LEADER);
        mockLeader.setTeam(mockTeam);

        mockInvitedUser = new DefaultUser(2L);
        mockInvitedUser.setName("Luigi");
        mockInvitedUser.setRole(UserRole.USER_NO_TEAM);
        mockInvitedUser.setInvitations(new ArrayList<>());

        mockInvitation = new Invitation();
        mockInvitation.setId(100L);
        mockInvitation.setFromTeam(mockTeam);
        mockInvitation.setToUser(mockInvitedUser);
        mockInvitation.setStatus(InvitationStatus.PENDING);
    }

    // TEST METODO: sendInvitation

    @Test
    void sendInvitation_Success() {
        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(defaultUserRepository.getById(1L)).thenReturn(mockLeader);
        when(defaultUserRepository.getById(2L)).thenReturn(mockInvitedUser);
        when(invitationRepository.create(any(Invitation.class))).thenAnswer(i -> i.getArgument(0));

        InvitationResponseDTO result = invitationService.sendInvitation(validCreateRequest);

        assertNotNull(result);
        assertEquals(InvitationStatus.PENDING, result.status());
        verify(invitationRepository, times(1)).create(any(Invitation.class));
        verify(defaultUserRepository, times(1)).update(mockInvitedUser); // L'utente viene aggiornato con l'invito
    }

    @Test
    void sendInvitation_InviterNotLeader_ThrowsException() {
        mockLeader.setRole(UserRole.TEAM_MEMBER); // Non è un leader

        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(defaultUserRepository.getById(1L)).thenReturn(mockLeader);
        when(defaultUserRepository.getById(2L)).thenReturn(mockInvitedUser);

        assertThrows(IllegalStateException.class, () -> invitationService.sendInvitation(validCreateRequest));
        verify(invitationRepository, never()).create(any());
    }

    @Test
    void sendInvitation_InvitedUserAlreadyInTeam_ThrowsException() {
        mockInvitedUser.setRole(UserRole.TEAM_MEMBER); // Ha già un team

        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(defaultUserRepository.getById(1L)).thenReturn(mockLeader);
        when(defaultUserRepository.getById(2L)).thenReturn(mockInvitedUser);

        assertThrows(IllegalStateException.class, () -> invitationService.sendInvitation(validCreateRequest));
    }

    @Test
    void sendInvitation_TeamFullForHackathon_ThrowsException() {
        Hackathon mockHackathon = new Hackathon();
        mockHackathon.setMaxDimensionOfTeam(1); // Limite di 1 membro
        mockTeam.setSubscribedHackathon(mockHackathon);
        mockTeam.getMembers().add(mockLeader); // Il team ha già 1 membro (è pieno)

        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(defaultUserRepository.getById(1L)).thenReturn(mockLeader);
        when(defaultUserRepository.getById(2L)).thenReturn(mockInvitedUser);

        assertThrows(IllegalStateException.class, () -> invitationService.sendInvitation(validCreateRequest));
    }

    // TEST METODO: getAllInvitationsByUserId

    @Test
    void getAllInvitationsByUserId_Success() {
        // Creiamo due inviti, di cui solo uno rivolto al mockInvitedUser (ID 2L)
        Invitation inv1 = new Invitation();
        inv1.setToUser(mockInvitedUser);
        inv1.setFromTeam(mockTeam);

        Invitation inv2 = new Invitation();
        inv2.setToUser(new DefaultUser(99L)); // Utente diverso
        inv2.setFromTeam(mockTeam);

        when(invitationRepository.getAll()).thenReturn(List.of(inv1, inv2));

        List<InvitationResponseDTO> result = invitationService.getAllInvitationsByUserId(2L);

        assertEquals(1, result.size());
        assertEquals("Luigi", result.get(0).forUserName());
    }

    @Test
    void getAllInvitationsByUserId_NullId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> invitationService.getAllInvitationsByUserId(null));
    }

    // TEST METODO: respondToInvitation

    @Test
    void respondToInvitation_Accept_Success() {
        RespondInvitationDTO request = new RespondInvitationDTO(100L, true);

        when(invitationRepository.getById(100L)).thenReturn(mockInvitation);
        when(defaultUserRepository.getById(2L)).thenReturn(mockInvitedUser);
        when(invitationRepository.update(any(Invitation.class))).thenAnswer(i -> i.getArgument(0));

        InvitationResponseDTO result = invitationService.respondToInvitation(request);

        assertNotNull(result);
        assertEquals(InvitationStatus.ACCEPTED, result.status());
        assertEquals(UserRole.TEAM_MEMBER, mockInvitedUser.getRole()); // L'utente è diventato membro
        assertEquals(mockTeam, mockInvitedUser.getTeam()); // L'utente è stato assegnato al team

        verify(teamRepository, times(1)).update(mockTeam);
        verify(defaultUserRepository, times(1)).update(mockInvitedUser);
    }

    @Test
    void respondToInvitation_Reject_Success() {
        RespondInvitationDTO request = new RespondInvitationDTO(100L, false); // false = rifiuta

        when(invitationRepository.getById(100L)).thenReturn(mockInvitation);
        when(defaultUserRepository.getById(2L)).thenReturn(mockInvitedUser);
        when(invitationRepository.update(any(Invitation.class))).thenAnswer(i -> i.getArgument(0));

        InvitationResponseDTO result = invitationService.respondToInvitation(request);

        assertNotNull(result);
        assertEquals(InvitationStatus.REJECTED, result.status());
        assertEquals(UserRole.USER_NO_TEAM, mockInvitedUser.getRole()); // L'utente resta senza team

        verify(teamRepository, never()).update(any()); // Il team non subisce modifiche
    }

    @Test
    void respondToInvitation_AlreadyHandled_ThrowsException() {
        RespondInvitationDTO request = new RespondInvitationDTO(100L, true);
        mockInvitation.setStatus(InvitationStatus.ACCEPTED); // Invito non più pendente

        when(invitationRepository.getById(100L)).thenReturn(mockInvitation);

        assertThrows(IllegalStateException.class, () -> invitationService.respondToInvitation(request));
    }

    @Test
    void respondToInvitation_Accept_AlreadyInTeam_ThrowsException() {
        RespondInvitationDTO request = new RespondInvitationDTO(100L, true);
        mockInvitedUser.setRole(UserRole.TEAM_MEMBER); // L'utente ha già accettato un altro invito nel frattempo

        when(invitationRepository.getById(100L)).thenReturn(mockInvitation);
        when(defaultUserRepository.getById(2L)).thenReturn(mockInvitedUser);

        assertThrows(IllegalStateException.class, () -> invitationService.respondToInvitation(request));
    }

    @Test
    void respondToInvitation_Accept_TeamInHackathon_ThrowsException() {
        RespondInvitationDTO request = new RespondInvitationDTO(100L, true);
        mockTeam.setSubscribedHackathon(new Hackathon()); // Il team è attualmente iscritto a un hackathon

        when(invitationRepository.getById(100L)).thenReturn(mockInvitation);
        when(defaultUserRepository.getById(2L)).thenReturn(mockInvitedUser);

        assertThrows(IllegalStateException.class, () -> invitationService.respondToInvitation(request));
    }
}