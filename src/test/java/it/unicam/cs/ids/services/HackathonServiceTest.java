package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.CreateHackathonDTO;
import it.unicam.cs.ids.dtos.HackathonResponseDTO;
import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.models.StaffUser;
import it.unicam.cs.ids.models.utils.HackathonStatus;
import it.unicam.cs.ids.models.utils.StaffRole;
import it.unicam.cs.ids.repositories.abstractions.IHackathonRepository;
import it.unicam.cs.ids.repositories.abstractions.IStaffUserRepository;
import it.unicam.cs.ids.validators.abstractions.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HackathonServiceTest {

    @Mock
    private IHackathonRepository hackathonRepository;

    @Mock
    private IStaffUserRepository staffUserRepository;

    @Mock
    private Validator<CreateHackathonDTO> hackathonValidator;

    // InjectMocks crea un'istanza di HackathonService passandogli i mock qui sopra nel costruttore
    @InjectMocks
    private HackathonService hackathonService;

    private CreateHackathonDTO validRequest;
    private StaffUser mockOrganizer;
    private StaffUser mockJudge;
    private StaffUser mockMentor;

    @BeforeEach
    void setUp() {
        // Prepariamo i dati fittizi che useremo nei test
        mockOrganizer = new StaffUser(1L, StaffRole.ORGANIZER);
        mockJudge = new StaffUser(2L, StaffRole.JUDGE);
        mockMentor = new StaffUser(3L, StaffRole.MENTOR);

        validRequest = new CreateHackathonDTO(
                1L, "EcoHack",
                LocalDateTime.now().plusDays(10), LocalDateTime.now().plusDays(12),
                LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(12),
                "Regolamento...", 5000.0, "Roma", 5,
                2L, List.of(3L)
        );
    }

    @Test
    void addHackathon_Success() {
        // Arrange: Diciamo a Mockito come devono rispondere i mock
        doNothing().when(hackathonValidator).validate(validRequest);
        when(staffUserRepository.getById(1L)).thenReturn(mockOrganizer);
        when(staffUserRepository.getById(2L)).thenReturn(mockJudge);
        when(staffUserRepository.getById(3L)).thenReturn(mockMentor);

        Hackathon expectedHackathon = new Hackathon();
        expectedHackathon.setName("EcoHack");
        when(hackathonRepository.create(any(Hackathon.class))).thenReturn(expectedHackathon);

        // Act: Eseguiamo il metodo da testare
        Hackathon result = hackathonService.addHackathon(validRequest);

        // Assert: Verifichiamo che il risultato sia corretto
        assertNotNull(result);
        assertEquals("EcoHack", result.getName());

        // Verifichiamo che i metodi dei repository siano stati chiamati il numero giusto di volte
        verify(hackathonValidator, times(1)).validate(validRequest);
        verify(staffUserRepository, times(3)).getById(anyLong());
        verify(hackathonRepository, times(1)).create(any(Hackathon.class));
    }

    @Test
    void addHackathon_OrganizerNotFound_ThrowsException() {
        // Arrange
        doNothing().when(hackathonValidator).validate(validRequest);
        // Simuliamo che il repository non trovi l'organizzatore
        when(staffUserRepository.getById(1L)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            hackathonService.addHackathon(validRequest);
        });

        assertEquals("Organizer not found in the system", exception.getMessage());

        // Assicuriamoci che non si arrivi mai a chiamare il create sul DB se fallisce prima
        verify(hackathonRepository, never()).create(any());
    }

    @Test
    void addHackathon_ValidatorFails_ThrowsException() {
        // Arrange: Simuliamo che il validatore lanci un'eccezione (es. DTO invalido)
        doThrow(new IllegalArgumentException("Data invalida"))
                .when(hackathonValidator).validate(validRequest);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            hackathonService.addHackathon(validRequest);
        });

        assertEquals("Data invalida", exception.getMessage());
        verify(staffUserRepository, never()).getById(anyLong());
        verify(hackathonRepository, never()).create(any());
    }

    @Test
    void getAllHackathons_Success() {
        // Arrange: Prepariamo gli utenti dello staff con nome e cognome
        StaffUser organizer = new StaffUser(1L, StaffRole.ORGANIZER);
        organizer.setName("Mario");
        organizer.setSurname("Rossi");

        StaffUser judge = new StaffUser(2L, StaffRole.JUDGE);
        judge.setName("Luigi");
        judge.setSurname("Verdi");

        StaffUser mentor = new StaffUser(3L, StaffRole.MENTOR);
        mentor.setName("Anna");
        mentor.setSurname("Bianchi");

        // Prepariamo l'entitÃ  Hackathon simulata dal DB
        Hackathon hackathon = new Hackathon();
        hackathon.setId(100L);
        hackathon.setName("Spring Hack");
        hackathon.setCashPrize(1000.0);
        hackathon.setStatus(HackathonStatus.REGISTRATION);
        hackathon.setOrganizer(organizer);
        hackathon.setJudge(judge);
        hackathon.setMentors(List.of(mentor));

        // Istruiamo il finto repository a restituire la nostra lista
        when(hackathonRepository.getAll()).thenReturn(List.of(hackathon));

        // Act: Chiamiamo il metodo del service
        List<HackathonResponseDTO> result = hackathonService.getAllHackathons();

        // Assert: Verifichiamo che la lista non sia nulla e contenga i dati mappati correttamente
        assertNotNull(result);
        assertEquals(1, result.size());

        HackathonResponseDTO dto = result.get(0);
        assertEquals(100L, dto.id());
        assertEquals("Spring Hack", dto.name());
        assertEquals(1000.0, dto.cashPrize());
        assertEquals(HackathonStatus.REGISTRATION, dto.status());

        // Verifichiamo la corretta concatenazione di nome e cognome
        assertEquals("Mario Rossi", dto.organizerName());
        assertEquals("Luigi Verdi", dto.judgeName());

        // Verifichiamo la lista dei mentori
        assertNotNull(dto.mentorNames());
        assertEquals(1, dto.mentorNames().size());
        assertEquals("Anna Bianchi", dto.mentorNames().get(0));

        // Verifichiamo che il repository sia stato interrogato
        verify(hackathonRepository, times(1)).getAll();
    }

    @Test
    void getAllHackathons_EmptyList_ReturnsNull() {
        // Arrange: Il repository restituisce una lista vuota
        when(hackathonRepository.getAll()).thenReturn(List.of());

        // Act
        List<HackathonResponseDTO> result = hackathonService.getAllHackathons();

        // Assert: Il service deve restituire null (come da tua logica)
        assertNull(result);
        verify(hackathonRepository, times(1)).getAll();
    }
}