package it.unicam.cs.ids;

import it.unicam.cs.ids.controllers.TeamController;
import it.unicam.cs.ids.dtos.CreateHackathonDTO;
import it.unicam.cs.ids.dtos.CreateTeamDTO;
import it.unicam.cs.ids.dtos.HackathonResponseDTO;
import it.unicam.cs.ids.models.DefaultUser;
import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.models.StaffUser;
import it.unicam.cs.ids.models.Team;
import it.unicam.cs.ids.models.utils.StaffRole;
import it.unicam.cs.ids.models.utils.UserRole;
import it.unicam.cs.ids.repositories.DefaultUserRepository;
import it.unicam.cs.ids.repositories.HackathonRepository;
import it.unicam.cs.ids.repositories.StaffUserRepository;
import it.unicam.cs.ids.repositories.TeamRepository;
import it.unicam.cs.ids.repositories.abstractions.IDefaultUserRepository;
import it.unicam.cs.ids.repositories.abstractions.IHackathonRepository;
import it.unicam.cs.ids.repositories.abstractions.IStaffUserRepository;
import it.unicam.cs.ids.repositories.abstractions.ITeamRepository;
import it.unicam.cs.ids.services.HackathonService;
import it.unicam.cs.ids.services.TeamService;
import it.unicam.cs.ids.services.abstractions.IHackathonService;
import it.unicam.cs.ids.services.abstractions.ITeamService;
import it.unicam.cs.ids.validators.HackathonValidator;
import it.unicam.cs.ids.validators.TeamValidator;
import it.unicam.cs.ids.validators.abstractions.Validator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // 0. Inizializziamo il database UNA SOLA VOLTA per tutta l'app
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("HackHubPU");
        EntityManager em = emf.createEntityManager();

        // 1. "Dependency Injection" manuale (Quello che farà Spring in futuro)
        IStaffUserRepository staffRepo = new StaffUserRepository(em);
        IHackathonRepository hackRepo = new HackathonRepository(em);
        Validator<Hackathon> validator = new HackathonValidator();
        IHackathonService hackathonService = new HackathonService(hackRepo, staffRepo, validator);

        // NUOVE DIPENDENZE PER IL TEAM
        IDefaultUserRepository defaultUserRepo = new DefaultUserRepository(em);
        ITeamRepository teamRepo = new TeamRepository(em);
        Validator<Team> teamValidator = new TeamValidator();
        ITeamService teamService = new TeamService(teamRepo, defaultUserRepo, teamValidator);
        TeamController teamController = new TeamController(teamService);

        try {
            StaffUser organizer = new StaffUser();
            organizer.setName("Mario");
            organizer.setSurname("Rossi");
            organizer.setEmail("mario.rossi@hackhub.it");
            organizer.setRole(StaffRole.ORGANIZER);
            organizer = staffRepo.create(organizer); // Il DB salva e ci restituisce l'utente con l'ID valorizzato

            StaffUser judge = new StaffUser();
            judge.setName("Ada");
            judge.setSurname("Lovelace");
            judge.setRole(StaffRole.JUDGE);
            judge = staffRepo.create(judge);

            StaffUser mentor = new StaffUser();
            mentor.setName("Alan");
            mentor.setSurname("Turing");
            mentor.setRole(StaffRole.MENTOR);
            mentor = staffRepo.create(mentor);

            System.out.println("Staff creato! ID Organizzatore: " + organizer.getId() +
                    ", ID Giudice: " + judge.getId() +
                    ", ID Mentore: " + mentor.getId());

            System.out.println("\n--- 2. Simulazione Creazione Hackathon da DTO ---");

            // Creiamo la richiesta passando l'organizer.getId() come primo parametro
            CreateHackathonDTO request = new CreateHackathonDTO(
                    organizer.getId(),
                    "HackHub Java Championship",
                    LocalDateTime.now().plusDays(10),
                    LocalDateTime.now().plusDays(12),
                    LocalDateTime.now().plusDays(5),
                    LocalDateTime.now().plusDays(11),
                    "Vietato l'uso di AI non autorizzate",
                    1500.0,
                    "Milano",
                    4,
                    judge.getId(),
                    List.of(mentor.getId())
            );

            //Logica di business
            Hackathon createdHackathon = hackathonService.addHackathon(request);

            System.out.println("\n SUCCESSO! Hackathon salvato nel Database.");
            System.out.println("ID Hackathon: " + createdHackathon.getId());
            System.out.println("Nome: " + createdHackathon.getName());
            System.out.println("Status: " + createdHackathon.getStatus());
            System.out.println("Organizzatore: " + createdHackathon.getOrganizer().getName());
            System.out.println("Mentori assegnati: " + createdHackathon.getMentors().size());

            System.out.println("\n Test getAllHackathons() ");
            List<HackathonResponseDTO> allHackathons = hackathonService.getAllHackathons();
            for (HackathonResponseDTO hackathon : allHackathons) {
                System.out.println("Hackathon: " + hackathon.name());
                System.out.println("Status: " + hackathon.status());
                System.out.println("Organizzatore: " + hackathon.organizerName());
                System.out.println("Giudici assegnati: " + hackathon.judgeName());
                System.out.println("Mentori assegnati: " + hackathon.mentorNames());
                System.out.println("----------------------------------------");
            }

            // --- INIZIO NUOVO CODICE TEAM ---
            System.out.println("\n--- 3. Simulazione Creazione Team ---");

            // Creiamo un utente "normale" e lo salviamo nel database
            DefaultUser standardUser = new DefaultUser();
            standardUser.setName("Luigi");
            standardUser.setSurname("Verdi");
            standardUser.setEmail("luigi.verdi@hackhub.it");
            standardUser.setPassword("password123");
            standardUser.setRole(UserRole.USER_NO_TEAM); // Inizialmente non ha un team
            standardUser = defaultUserRepo.create(standardUser);

            System.out.println("Utente Base creato! ID: " + standardUser.getId() + " - Ruolo: " + standardUser.getRole());

            // Prepariamo la richiesta per creare un nuovo team
            CreateTeamDTO teamRequest = new CreateTeamDTO(
                    standardUser.getId(),
                    "The Spring Booters"
            );

            // Passiamo la richiesta al controller per eseguire la logica
            Team createdTeam = teamController.createTeam(teamRequest);

            System.out.println("\n SUCCESSO! Team salvato nel Database.");
            System.out.println("ID Team: " + createdTeam.getId());
            System.out.println("Nome Team: " + createdTeam.getName());
            System.out.println("Membri Iniziali: " + createdTeam.getMembers().size() + " (" + createdTeam.getMembers().get(0).getName() + ")");

            // Verifica Aggiornamento Utente: ricarichiamo l'utente dal DB per assicurarci che sia stato aggiornato
            DefaultUser updatedUser = defaultUserRepo.getById(standardUser.getId());
            System.out.println("Verifica aggiornamento utente...");
            System.out.println("Nuovo Ruolo dell'utente: " + updatedUser.getRole()); // Dovrebbe essere TEAM_MEMBER
            System.out.println("Il team assegnato all'utente e': " + updatedUser.getTeam().getName());
            // --- FINE NUOVO CODICE TEAM ---

            Team team = teamRepo.getById(createdTeam.getId());
            System.out.println("\n--- 4. Simulazione Aggiornamento Team ---");
            team.setName("The Spring Booters 2.0");
            teamRepo.update(team);
            System.out.println("SUCCESSO! Team aggiornato nel Database." + " - Nome: " + teamRepo.getById(createdTeam.getId()).getName());

        } catch (Exception e) {
            System.err.println("\nERRORE: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Forza l'uscita per chiudere i thread in background di Hibernate
            System.exit(0);
        }
    }
}