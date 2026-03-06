package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.CreateHackathonDTO;
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
    private Validator<Hackathon> hackathonValidator;

    @Mock
    private IStaffUserRepository staffUserRepository;

    @InjectMocks
    private HackathonService hackathonService;

    private CreateHackathonDTO validRequest;

    @BeforeEach
    void setUp() {
        // Aggiunto 1L come organizerId per riflettere il nuovo DTO
        validRequest = new CreateHackathonDTO(
                1L, // organizerId
                "HackHub Global 2026",
                LocalDateTime.now().plusDays(30),
                LocalDateTime.now().plusDays(32),
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(31),
                "Regolamento rigoroso",
                10000.0,
                "Milano",
                4,
                10L,              // judgeId
                List.of(20L, 21L) // mentorsIds
        );
    }

    // Metodo helper privato per istruire il Mock sugli utenti da restituire
    private void mockStaffUsersExistence() {
        StaffUser dummyOrganizer = new StaffUser(1L, StaffRole.ORGANIZER);
        StaffUser dummyJudge = new StaffUser(10L, StaffRole.JUDGE);
        StaffUser dummyMentor1 = new StaffUser(20L, StaffRole.MENTOR);
        StaffUser dummyMentor2 = new StaffUser(21L, StaffRole.MENTOR);

        // Quando il service chiederà al database (finto) questi ID, noi gli restituiamo gli oggetti
        when(staffUserRepository.getById(1L)).thenReturn(dummyOrganizer);
        when(staffUserRepository.getById(10L)).thenReturn(dummyJudge);
        when(staffUserRepository.getById(20L)).thenReturn(dummyMentor1);
        when(staffUserRepository.getById(21L)).thenReturn(dummyMentor2);
    }

    @Test
    void addHackathon_ShouldCreateAndReturnHackathon_WhenValid() {
        // Arrange: prepariamo i mock
        mockStaffUsersExistence(); // Diciamo al mock che gli utenti esistono
        when(hackathonValidator.validate(any(Hackathon.class))).thenReturn(true);
        when(hackathonRepository.create(any(Hackathon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Hackathon result = hackathonService.addHackathon(validRequest);

        // Assert
        assertNotNull(result);
        assertEquals("HackHub Global 2026", result.getName());
        assertEquals(HackathonStatus.REGISTRATION, result.getStatus());
        assertEquals(10000.0, result.getCashPrize());

        // Verifica dei ruoli creati
        assertNotNull(result.getOrganizer());
        assertEquals(1L, result.getOrganizer().getId());
        assertEquals(10L, result.getJudge().getId());
        assertEquals(2, result.getMentors().size());

        // Verifica interazioni
        verify(staffUserRepository, times(4)).getById(anyLong()); // 1 org + 1 judge + 2 mentors
        verify(hackathonValidator, times(1)).validate(any(Hackathon.class));
        verify(hackathonRepository, times(1)).create(any(Hackathon.class));
    }

    @Test
    void addHackathon_ShouldThrowException_WhenValidationFails() {
        // Arrange
        mockStaffUsersExistence(); // Gli utenti devono esistere, altrimenti l'errore verrebbe lanciato prima della validazione!
        when(hackathonValidator.validate(any(Hackathon.class))).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            hackathonService.addHackathon(validRequest);
        });

        assertEquals("Hackathon creation failed: invalid data", exception.getMessage());

        // Verifica che il salvataggio NON avvenga mai
        verify(hackathonRepository, never()).create(any(Hackathon.class));
    }

    @Test
    void addHackathon_ShouldThrowException_WhenOrganizerDoesNotExist() {
        // Arrange: diciamo esplicitamente che per l'ID 1 il repository restituisce null
        when(staffUserRepository.getById(1L)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            hackathonService.addHackathon(validRequest);
        });

        assertEquals("Organizzatore non trovato nel sistema", exception.getMessage());

        // Verifica che ci si fermi subito e non si validi o salvi niente
        verify(hackathonValidator, never()).validate(any());
        verify(hackathonRepository, never()).create(any());
    }
}