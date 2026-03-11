package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.CreateSubmissionDTO;
import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.models.Submission;
import it.unicam.cs.ids.models.Team;
import it.unicam.cs.ids.models.utils.HackathonStatus;
import it.unicam.cs.ids.repositories.HackathonRepository;
import it.unicam.cs.ids.repositories.SubmissionRepository;
import it.unicam.cs.ids.repositories.TeamRepository;
import it.unicam.cs.ids.validators.CreateSubmissionValidator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SubmissionServiceTest {

    private static EntityManagerFactory emf;
    private EntityManager em;

    // Repository e Service da testare
    private TeamRepository teamRepo;
    private HackathonRepository hackRepo;
    private SubmissionRepository submissionRepo;
    private SubmissionService submissionService;

    // Dati finti da usare nei test
    private Team testTeam;
    private Hackathon testHackathon;

    @BeforeAll
    static void initAll() {
        // Viene eseguito UNA SOLA VOLTA per tutta la classe di test
        emf = Persistence.createEntityManagerFactory("HackHubPU");
    }

    @BeforeEach
    void setUp() {
        // Viene eseguito PRIMA di OGNI SINGOLO @Test
        em = emf.createEntityManager();

        teamRepo = new TeamRepository(em);
        hackRepo = new HackathonRepository(em);
        submissionRepo = new SubmissionRepository(em);

        submissionService = new SubmissionService(
                teamRepo, hackRepo, submissionRepo, new CreateSubmissionValidator()
        );

        // Prepariamo i dati di base nel DB (un Hackathon e un Team iscritto)
        testHackathon = new Hackathon();
        testHackathon.setName("Test Hackathon " + System.currentTimeMillis()); // Evitiamo problemi anche qui!
        testHackathon.setSubmitDeadline(LocalDateTime.now().plusDays(5));
        testHackathon.setStatus(HackathonStatus.REGISTRATION);
        testHackathon = hackRepo.create(testHackathon);

        testTeam = new Team();
        // Aggiungiamo un identificativo univoco (il millisecondo attuale) al nome
        testTeam.setName("Test Team " + System.currentTimeMillis());
        testTeam.setSubscribedHackathon(testHackathon);
        testTeam = teamRepo.create(testTeam);
    }

    @AfterEach
    void tearDown() {
        // Puliamo l'EntityManager DOPO ogni test per isolarli l'uno dall'altro
        if (em.isOpen()) {
            em.close();
        }
    }

    @AfterAll
    static void tearDownAll() {
        if (emf.isOpen()) {
            emf.close();
        }
    }

    // --- I NOSTRI TEST ---

    @Test
    @DisplayName("Dovrebbe creare una sottomissione con successo se i dati sono validi")
    void shouldCreateSubmissionSuccessfully() {
        // Arrange (Preparo i dati)
        CreateSubmissionDTO request = new CreateSubmissionDTO(
                testTeam.getId(),
                testHackathon.getId(),
                "https://github.com/test",
                "Descrizione Test",
                null
        );

        // Act (Eseguo il metodo da testare)
        Submission result = submissionService.addSubmission(request);

        // Assert (Verifico che il risultato sia quello atteso)
        assertNotNull(result.getId(), "L'ID della sottomissione non dovrebbe essere nullo (è stata salvata)");
        assertEquals("https://github.com/test", result.getProjectUrl());
        assertEquals(testTeam.getId(), result.getTeam().getId());
    }

    @Test
    @DisplayName("Dovrebbe lanciare eccezione se la scadenza è passata")
    void shouldThrowExceptionWhenDeadlinePassed() {
        // Arrange: Modifichiamo l'hackathon per far scadere il tempo
        testHackathon.setSubmitDeadline(LocalDateTime.now().minusDays(1)); // Scaduto ieri!
        hackRepo.update(testHackathon);

        CreateSubmissionDTO request = new CreateSubmissionDTO(
                testTeam.getId(),
                testHackathon.getId(),
                "https://github.com/test",
                "Descrizione Test",
                null
        );

        // Act & Assert: Verifichiamo che il metodo lanci l'eccezione corretta
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> submissionService.addSubmission(request)
        );

        assertEquals("The submission deadline has already passed.", exception.getMessage());
    }
}