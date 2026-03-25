package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.requests.AddMentorDTO;
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
import java.util.ArrayList;
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
    private Hackathon mockHackathon;

    @BeforeEach
    void setUp() {
        lenient().when(unitOfWork.getStaffUserRepository()).thenReturn(staffUserRepository);
        lenient().when(unitOfWork.getHackathonRepository()).thenReturn(hackathonRepository);

        mockOrganizer = new StaffUser(1L, StaffRole.ORGANIZER);
        mockOrganizer.setName("Mario"); mockOrganizer.setSurname("Rossi");

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

        mockHackathon = new Hackathon();
        mockHackathon.setId(100L);
        mockHackathon.setName("EcoHack");
        mockHackathon.setOrganizer(mockOrganizer);
        mockHackathon.setJudge(mockJudge);
        mockHackathon.setMentors(new ArrayList<>(List.of(mockMentor)));
        mockHackathon.setStatus(HackathonStatus.REGISTRATION);
    }

    // --- TEST METODO addHackathon ---

    @Test
    void addHackathon_Success() {
        doNothing().when(hackathonValidator).validate(validRequest);
        when(staffUserRepository.getById(1L)).thenReturn(mockOrganizer);
        when(staffUserRepository.getById(2L)).thenReturn(mockJudge);
        when(staffUserRepository.getById(3L)).thenReturn(mockMentor);

        HackathonResponseDTO result = hackathonService.addHackathon(validRequest);

        assertNotNull(result);
        assertEquals("EcoHack", result.name());
        verify(hackathonRepository, times(1)).create(any(Hackathon.class));
    }

    @Test
    void addHackathon_ValidatorFails_ThrowsException() {
        doThrow(new IllegalArgumentException("Data invalida")).when(hackathonValidator).validate(validRequest);
        assertThrows(IllegalArgumentException.class, () -> hackathonService.addHackathon(validRequest));
        verify(hackathonRepository, never()).create(any());
    }

    @Test
    void addHackathon_OrganizerNotFound_ThrowsException() {
        doNothing().when(hackathonValidator).validate(validRequest);
        when(staffUserRepository.getById(1L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> hackathonService.addHackathon(validRequest));
    }

    @Test
    void addHackathon_JudgeNotFound_ThrowsException() {
        doNothing().when(hackathonValidator).validate(validRequest);
        when(staffUserRepository.getById(1L)).thenReturn(mockOrganizer);
        when(staffUserRepository.getById(2L)).thenReturn(null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> hackathonService.addHackathon(validRequest));
        assertEquals("Judge not found in the system.", ex.getMessage());
    }

    @Test
    void addHackathon_MentorNotFound_ThrowsException() {
        doNothing().when(hackathonValidator).validate(validRequest);
        when(staffUserRepository.getById(1L)).thenReturn(mockOrganizer);
        when(staffUserRepository.getById(2L)).thenReturn(mockJudge);
        when(staffUserRepository.getById(3L)).thenReturn(null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> hackathonService.addHackathon(validRequest));
        assertTrue(ex.getMessage().contains("Mentor with ID 3 not found"));
    }

    @Test
    void addHackathon_EmptyMentorsList_ThrowsException() {
        // Implementato assumendo che tu aggiunga il controllo nel Service: if(request.mentorsIdS().isEmpty()) throw ...
        CreateHackathonDTO emptyMentorsRequest = new CreateHackathonDTO(
                1L, "EcoHack", LocalDateTime.now().plusDays(10), LocalDateTime.now().plusDays(12),
                LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(12),
                "Regolamento...", 5000.0, "Roma", 5, 2L, List.of()
        );
        doNothing().when(hackathonValidator).validate(emptyMentorsRequest);
        when(staffUserRepository.getById(1L)).thenReturn(mockOrganizer);
        when(staffUserRepository.getById(2L)).thenReturn(mockJudge);

        assertThrows(IllegalArgumentException.class, () -> hackathonService.addHackathon(emptyMentorsRequest));
    }

    // --- TEST METODO getAllHackathons ---

    @Test
    void getAllHackathons_Success() {
        when(hackathonRepository.getAll()).thenReturn(List.of(mockHackathon));
        List<HackathonResponseDTO> result = hackathonService.getAllHackathons();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAllHackathons_EmptyList_ReturnsNull() {
        when(hackathonRepository.getAll()).thenReturn(List.of());
        assertNull(hackathonService.getAllHackathons());
    }

    // --- TEST METODO addMentorToHackathon ---

    @Test
    void addMentorToHackathon_Success() {
        StaffUser newMentor = new StaffUser(4L, StaffRole.MENTOR);
        newMentor.setName("Giacomo"); newMentor.setSurname("Poretti");

        // CORRETTO: organizerId=1L, hackathonId=100L, mentorId=4L
        AddMentorDTO request = new AddMentorDTO(1L, 100L, 4L);

        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);
        when(staffUserRepository.getById(4L)).thenReturn(newMentor);

        HackathonResponseDTO result = hackathonService.addMentorToHackathon(request);

        assertNotNull(result);
        assertEquals(2, mockHackathon.getMentors().size());
        verify(hackathonRepository, times(1)).update(mockHackathon);
    }

    @Test
    void addMentorToHackathon_HackathonNotFound_ThrowsException() {
        // CORRETTO: organizerId=1L, hackathonId=999L
        AddMentorDTO request = new AddMentorDTO(1L, 999L, 4L);
        when(hackathonRepository.getById(999L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> hackathonService.addMentorToHackathon(request));
    }

    @Test
    void addMentorToHackathon_InvalidStatus_ThrowsException() {
        mockHackathon.setStatus(HackathonStatus.FINISHED);
        // CORRETTO: organizerId=1L, hackathonId=100L
        AddMentorDTO request = new AddMentorDTO(1L, 100L, 4L);
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);
        assertThrows(IllegalStateException.class, () -> hackathonService.addMentorToHackathon(request));
    }

    @Test
    void addMentorToHackathon_WrongOrganizer_ThrowsException() {
        // CORRETTO: organizerId=99L (non è l'organizzatore reale), hackathonId=100L
        AddMentorDTO request = new AddMentorDTO(99L, 100L, 4L);
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);
        assertThrows(SecurityException.class, () -> hackathonService.addMentorToHackathon(request));
    }

    @Test
    void addMentorToHackathon_MentorNotFound_ThrowsException() {
        // CORRETTO: organizerId=1L, hackathonId=100L, mentorId=999L
        AddMentorDTO request = new AddMentorDTO(1L, 100L, 999L);
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);
        when(staffUserRepository.getById(999L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> hackathonService.addMentorToHackathon(request));
    }

    @Test
    void addMentorToHackathon_DuplicateMentor_ThrowsException() {
        // CORRETTO: organizerId=1L, hackathonId=100L, mentorId=3L (già mentore)
        AddMentorDTO request = new AddMentorDTO(1L, 100L, 3L);
        when(hackathonRepository.getById(100L)).thenReturn(mockHackathon);
        when(staffUserRepository.getById(3L)).thenReturn(mockMentor);
        assertThrows(IllegalStateException.class, () -> hackathonService.addMentorToHackathon(request));
    }
}