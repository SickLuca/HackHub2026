package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.requests.CreateHackathonDTO;
import it.unicam.cs.ids.dtos.responses.HackathonResponseDTO;
import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.models.StaffUser;
import it.unicam.cs.ids.models.utils.HackathonStatus;
import it.unicam.cs.ids.models.utils.StaffRole;
import it.unicam.cs.ids.repositories.abstractions.IHackathonRepository;
import it.unicam.cs.ids.repositories.abstractions.IStaffUserRepository;
import it.unicam.cs.ids.utils.unitOfWork.IUnitOfWork;
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
    private IUnitOfWork unitOfWork;

    @Mock
    private IHackathonRepository hackathonRepository;

    @Mock
    private IStaffUserRepository staffUserRepository;

    @Mock
    private Validator<CreateHackathonDTO> hackathonValidator;

    @InjectMocks
    private HackathonService hackathonService;

    private CreateHackathonDTO validRequest;
    private StaffUser mockOrganizer;
    private StaffUser mockJudge;
    private StaffUser mockMentor;

    @BeforeEach
    void setUp() {
        // Mockiamo la UnitOfWork affinché restituisca i nostri repository mockati
        lenient().when(unitOfWork.getStaffUserRepository()).thenReturn(staffUserRepository);
        lenient().when(unitOfWork.getHackathonRepository()).thenReturn(hackathonRepository);

        mockOrganizer = new StaffUser(1L, StaffRole.ORGANIZER);
        mockOrganizer.setName("Mario"); mockOrganizer.setSurname("Rossi"); // Necessari per il DTO

        mockJudge = new StaffUser(2L, StaffRole.JUDGE);
        mockJudge.setName("Luigi"); mockJudge.setSurname("Verdi");

        mockMentor = new StaffUser(3L, StaffRole.MENTOR);
        mockMentor.setName("Anna"); mockMentor.setSurname("Bianchi");

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
        doNothing().when(hackathonValidator).validate(validRequest);
        when(staffUserRepository.getById(1L)).thenReturn(mockOrganizer);
        when(staffUserRepository.getById(2L)).thenReturn(mockJudge);
        when(staffUserRepository.getById(3L)).thenReturn(mockMentor);

        HackathonResponseDTO result = hackathonService.addHackathon(validRequest);

        assertNotNull(result);
        assertEquals("EcoHack", result.name());
        assertEquals("Mario Rossi", result.organizerName());

        verify(hackathonValidator, times(1)).validate(validRequest);
        verify(staffUserRepository, times(3)).getById(anyLong());
        verify(hackathonRepository, times(1)).create(any(Hackathon.class));
    }

    @Test
    void addHackathon_OrganizerNotFound_ThrowsException() {
        doNothing().when(hackathonValidator).validate(validRequest);
        when(staffUserRepository.getById(1L)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            hackathonService.addHackathon(validRequest);
        });

        assertEquals("Organizer not found in the system", exception.getMessage());
        verify(hackathonRepository, never()).create(any());
    }

    @Test
    void addHackathon_ValidatorFails_ThrowsException() {
        doThrow(new IllegalArgumentException("Data invalida"))
                .when(hackathonValidator).validate(validRequest);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            hackathonService.addHackathon(validRequest);
        });

        assertEquals("Data invalida", exception.getMessage());
        verify(staffUserRepository, never()).getById(anyLong());
        verify(hackathonRepository, never()).create(any());
    }

    @Test
    void getAllHackathons_Success() {
        Hackathon hackathon = new Hackathon();
        hackathon.setId(100L);
        hackathon.setName("Spring Hack");
        hackathon.setCashPrize(1000.0);
        hackathon.setStatus(HackathonStatus.REGISTRATION);
        hackathon.setOrganizer(mockOrganizer);
        hackathon.setJudge(mockJudge);
        hackathon.setMentors(List.of(mockMentor));

        when(hackathonRepository.getAll()).thenReturn(List.of(hackathon));

        List<HackathonResponseDTO> result = hackathonService.getAllHackathons();

        assertNotNull(result);
        assertEquals(1, result.size());

        HackathonResponseDTO dto = result.get(0);
        assertEquals(100L, dto.id());
        assertEquals("Spring Hack", dto.name());
        assertEquals("Mario Rossi", dto.organizerName());
        assertEquals("Anna Bianchi", dto.mentorNames().get(0));

        verify(hackathonRepository, times(1)).getAll();
    }

    @Test
    void getAllHackathons_EmptyList_ReturnsNull() {
        when(hackathonRepository.getAll()).thenReturn(List.of());

        List<HackathonResponseDTO> result = hackathonService.getAllHackathons();

        assertNull(result);
        verify(hackathonRepository, times(1)).getAll();
    }
}