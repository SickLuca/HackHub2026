package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.requests.CreateSupportRequestDTO;
import it.unicam.cs.ids.dtos.responses.SupportRequestResponseDTO;
import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.models.StaffUser;
import it.unicam.cs.ids.models.SupportRequest;
import it.unicam.cs.ids.models.Team;
import it.unicam.cs.ids.models.utils.SupportRequestStatus;
import it.unicam.cs.ids.repositories.abstractions.IHackathonRepository;
import it.unicam.cs.ids.repositories.abstractions.ISupportRequestRepository;
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
class SupportRequestServiceTest {

    @Mock
    private IUnitOfWork unitOfWork;

    @Mock
    private ITeamRepository teamRepository;

    @Mock
    private IHackathonRepository hackathonRepository;

    @Mock
    private ISupportRequestRepository supportRequestRepository;

    @Mock
    private Validator<CreateSupportRequestDTO> validator;

    private SupportRequestService supportRequestService;

    // Entità Mock di base
    private CreateSupportRequestDTO validCreateRequest;
    private Team mockTeam;
    private Hackathon mockHackathon;
    private StaffUser mockMentor;

    @BeforeEach
    void setUp() {
        // Mock dei repository tramite la UoW in modo "lenient"
        lenient().when(unitOfWork.getTeamRepository()).thenReturn(teamRepository);
        lenient().when(unitOfWork.getHackathonRepository()).thenReturn(hackathonRepository);
        lenient().when(unitOfWork.getSupportRequestRepository()).thenReturn(supportRequestRepository);

        // Iniezione manuale e sicura del service
        supportRequestService = new SupportRequestService(unitOfWork, validator);

        // --- Setup Dati Standard ---
        validCreateRequest = new CreateSupportRequestDTO(10L, 100L, "Abbiamo un problema con l'API");

        mockMentor = new StaffUser();
        mockMentor.setId(50L);
        mockMentor.setName("Marco");

        mockHackathon = new Hackathon();
        mockHackathon.setId(100L);
        mockHackathon.setName("CodeFest 2026");
        // Assegniamo il mentore all'hackathon
        mockHackathon.setMentors(new ArrayList<>(List.of(mockMentor)));

        mockTeam = new Team();
        mockTeam.setId(10L);
        mockTeam.setName("Team Alpha");
        // Iscriviamo il team all'hackathon corretto
        mockTeam.setSubscribedHackathon(mockHackathon);
    }

    // TEST METODO: createRequest

    @Test
    void createRequest_Success() {
        // Nessuna richiesta PENDING presente nel database per questo hackathon
        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);
        when(supportRequestRepository.getByHackathonId(100L)).thenReturn(new ArrayList<>());
        when(supportRequestRepository.create(any(SupportRequest.class))).thenAnswer(i -> i.getArgument(0));

        SupportRequestResponseDTO result = supportRequestService.createRequest(validCreateRequest);

        assertNotNull(result);
        assertEquals("Abbiamo un problema con l'API", result.message());
        assertEquals("PENDING", result.status());
        verify(supportRequestRepository, times(1)).create(any(SupportRequest.class));
    }

    @Test
    void createRequest_TeamNotFound_ThrowsException() {
        when(teamRepository.getById(10L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> supportRequestService.createRequest(validCreateRequest));
        verify(supportRequestRepository, never()).create(any());
    }

    @Test
    void createRequest_HackathonNotFound_ThrowsException() {
        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(hackathonRepository.getById(100L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> supportRequestService.createRequest(validCreateRequest));
    }

    @Test
    void createRequest_TeamNotSubscribedToHackathon_ThrowsException() {
        Hackathon anotherHackathon = new Hackathon();
        anotherHackathon.setId(999L);
        mockTeam.setSubscribedHackathon(anotherHackathon); // Il team è iscritto ad un ALTRO hackathon

        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);

        assertThrows(IllegalStateException.class, () -> supportRequestService.createRequest(validCreateRequest));
    }

    @Test
    void createRequest_PendingRequestAlreadyExists_ThrowsException() {
        // Creiamo una richiesta già esistente in stato PENDING per lo stesso team
        SupportRequest existingRequest = new SupportRequest();
        existingRequest.setId(1L);
        existingRequest.setTeam(mockTeam);
        existingRequest.setStatus(SupportRequestStatus.PENDING);

        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);
        when(supportRequestRepository.getByHackathonId(100L)).thenReturn(List.of(existingRequest));

        assertThrows(IllegalStateException.class, () -> supportRequestService.createRequest(validCreateRequest));
        verify(supportRequestRepository, never()).create(any());
    }

    @Test
    void createRequest_ClosedRequestExists_Success() {
        // Se esiste una richiesta precedente ma è già chiusa (CLOSED), possiamo aprirne una nuova!
        SupportRequest oldRequest = new SupportRequest();
        oldRequest.setId(1L);
        oldRequest.setTeam(mockTeam);
        oldRequest.setStatus(SupportRequestStatus.CLOSED);

        when(teamRepository.getById(10L)).thenReturn(mockTeam);
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);
        when(supportRequestRepository.getByHackathonId(100L)).thenReturn(List.of(oldRequest));
        when(supportRequestRepository.create(any(SupportRequest.class))).thenAnswer(i -> i.getArgument(0));

        SupportRequestResponseDTO result = supportRequestService.createRequest(validCreateRequest);

        assertNotNull(result);
        assertEquals("PENDING", result.status()); // Viene creata con successo
    }

    // TEST METODO: getRequestsForHackathon

    @Test
    void getRequestsForHackathon_Success() {
        SupportRequest req1 = new SupportRequest();
        req1.setId(1L);
        req1.setTeam(mockTeam);
        req1.setHackathon(mockHackathon);
        req1.setMessage("Help 1");
        req1.setStatus(SupportRequestStatus.PENDING);
        req1.setCreatedAt(LocalDateTime.now());

        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);
        when(supportRequestRepository.getByHackathonId(100L)).thenReturn(List.of(req1));

        // Richiediamo i ticket con l'ID del mentore corretto (50L)
        List<SupportRequestResponseDTO> results = supportRequestService.getRequestsForHackathon(100L, 50L);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Help 1", results.get(0).message());
        assertEquals("Team Alpha", results.get(0).teamName());
    }

    @Test
    void getRequestsForHackathon_HackathonNotFound_ThrowsException() {
        when(hackathonRepository.getById(100L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> supportRequestService.getRequestsForHackathon(100L, 50L));
    }

    @Test
    void getRequestsForHackathon_UserNotAssignedMentor_ThrowsException() {
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);

        // Proviamo a passare l'ID di un utente che NON è tra i mentori assegnati (es. 999L)
        assertThrows(SecurityException.class, () -> supportRequestService.getRequestsForHackathon(100L, 999L));
    }
}