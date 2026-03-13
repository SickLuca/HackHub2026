package it.unicam.cs.ids;

import it.unicam.cs.ids.controllers.HackathonController;
import it.unicam.cs.ids.controllers.SubmissionController;
import it.unicam.cs.ids.controllers.TeamController;
import it.unicam.cs.ids.dtos.CreateHackathonDTO;
import it.unicam.cs.ids.dtos.CreateSubmissionDTO;
import it.unicam.cs.ids.dtos.CreateTeamDTO;
import it.unicam.cs.ids.repositories.*;
import it.unicam.cs.ids.repositories.abstractions.*;
import it.unicam.cs.ids.services.HackathonService;
import it.unicam.cs.ids.services.SubmissionService;
import it.unicam.cs.ids.services.TeamService;
import it.unicam.cs.ids.services.abstractions.IHackathonService;
import it.unicam.cs.ids.services.abstractions.ISubmissionService;
import it.unicam.cs.ids.services.abstractions.ITeamService;
import it.unicam.cs.ids.validators.CreateHackathonValidator;
import it.unicam.cs.ids.validators.CreateSubmissionValidator;
import it.unicam.cs.ids.validators.CreateTeamValidator;
import it.unicam.cs.ids.validators.abstractions.Validator;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class Main {

    public static void main(String[] args) {
        System.out.println("Avvio di HackHub in corso...");

        EntityManagerFactory emf = null;
        EntityManager em = null;

        try {
            // 1. Inizializzazione JPA e Connessione al Database
            // Utilizziamo il nome della Persistence Unit definita nel file persistence.xml
            emf = Persistence.createEntityManagerFactory("HackHubPU");
            em = emf.createEntityManager();
            System.out.println("Connessione al database stabilita con successo.");

            // 2. Dependency Injection Manuale (Setup Architettura)
            setupArchitecture(em);

            System.out.println("Architettura inizializzata correttamente.");
            System.out.println("L'applicazione Ã¨ pronta per ricevere richieste (es. tramite CLI o layer REST futuro).");


        } catch (Exception e) {
            System.err.println("Errore critico durante l'avvio dell'applicazione:");
            e.printStackTrace();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
            if (emf != null && emf.isOpen()) {
                emf.close();
            }
            System.out.println("Spegnimento di HackHub completato.");
        }
    }

    private static void setupArchitecture(EntityManager em) {
        // --- 1. Inizializzazione Repositories ---
        IStaffUserRepository staffRepo = new StaffUserRepository(em);
        IHackathonRepository hackRepo = new HackathonRepository(em);
        IDefaultUserRepository defaultUserRepo = new DefaultUserRepository(em);
        ITeamRepository teamRepo = new TeamRepository(em);
        ISubmissionRepository submissionRepo = new SubmissionRepository(em);

        // --- 2. Inizializzazione Validators ---
        Validator<CreateHackathonDTO> hackathonValidator = new CreateHackathonValidator();
        Validator<CreateTeamDTO> teamValidator = new CreateTeamValidator();
        Validator<CreateSubmissionDTO> submissionValidator = new CreateSubmissionValidator();

        // --- 3. Inizializzazione Services ---
        IHackathonService hackathonService = new HackathonService(hackRepo, staffRepo, hackathonValidator);
        ITeamService teamService = new TeamService(teamRepo, defaultUserRepo, teamValidator, hackRepo);
        ISubmissionService submissionService = new SubmissionService(teamRepo, hackRepo, submissionRepo, submissionValidator);

        // --- 4. Inizializzazione Controllers ---
        HackathonController hackathonController = new HackathonController(hackathonService);
        TeamController teamController = new TeamController(teamService);
        SubmissionController submissionController = new SubmissionController(submissionService);

        System.out.println("Controller istanziati e collegati ai rispettivi Service.");
    }
}
