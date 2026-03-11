package it.unicam.cs.ids;

import it.unicam.cs.ids.controllers.TeamController;
import it.unicam.cs.ids.dtos.*;
import it.unicam.cs.ids.models.*;
import it.unicam.cs.ids.models.utils.StaffRole;
import it.unicam.cs.ids.models.utils.UserRole;
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

import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // 0. Inizializziamo il database UNA SOLA VOLTA per tutta l'app
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("HackHubPU");
        EntityManager em = emf.createEntityManager();

        // 1. "Dependency Injection" manuale (Quello che farà Spring in futuro)
        IStaffUserRepository staffRepo = new StaffUserRepository(em);
        HackathonRepository hackRepo = new HackathonRepository(em);
        Validator<CreateHackathonDTO> validator = new CreateHackathonValidator();
        IHackathonService hackathonService = new HackathonService(hackRepo, staffRepo, validator);
        ISubmissionRepository submissionRepo = new SubmissionRepository(em);

        // NUOVE DIPENDENZE PER IL TEAM
        IDefaultUserRepository defaultUserRepo = new DefaultUserRepository(em);
        ITeamRepository teamRepo = new TeamRepository(em);
        Validator<CreateTeamDTO> teamValidator = new CreateTeamValidator();
        ITeamService teamService = new TeamService(teamRepo, defaultUserRepo, teamValidator, hackRepo, submissionRepo);
        TeamController teamController = new TeamController(teamService);

        Validator<CreateSubmissionDTO> submissionValidator = new CreateSubmissionValidator();
        ISubmissionService submissionService = new SubmissionService(teamRepo, hackRepo, submissionRepo, submissionValidator);

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


            // --- 5. Simulazione Iscrizione Team all'Hackathon ---
            System.out.println("\n--- 5. Simulazione Iscrizione Team all'Hackathon ---");

            // Usiamo il DTO creato in precedenza per iscrivere il team appena creato all'hackathon appena creato
            SubscribeTeamDTO enrollRequest = new SubscribeTeamDTO(
                    createdTeam.getId(),
                    createdHackathon.getId()
            );

            Team enrolledTeam = teamController.subscribeToHackathon(enrollRequest);

            System.out.println("SUCCESSO! Team iscritto all'Hackathon.");
            System.out.println("Il team '" + enrolledTeam.getName() + "' è ora ufficialmente iscritto all'hackathon: " + enrolledTeam.getSubscribedHackathon().getName());

            Team team2 = teamRepo.getById(enrolledTeam.getId());
            System.out.println(team2.getSubscribedHackathon().getName());
            // Verifica di un vincolo (Opzionale ma utile): proviamo a iscriverlo di nuovo per vedere se l'eccezione scatta
            try {
                System.out.println("Test: provo a re-iscrivere il team per verificare le regole di business...");
                teamController.subscribeToHackathon(enrollRequest);
            } catch (IllegalStateException ex) {
                System.out.println("Verifica superata! Il sistema ha bloccato la doppia iscrizione con il messaggio: " + ex.getMessage());
            }

            System.out.println("\n--- 6. Simulazione Sottomissione Progetto ---");

            CreateSubmissionDTO subRequest1 = new CreateSubmissionDTO(
                    enrolledTeam.getId(),
                    createdHackathon.getId(),
                    "https://github.com/spring-booters/hackhub",
                    "Ecco la nostra piattaforma per la gestione degli Hackathon!",
                    null // La data verrà generata automaticamente dal Service
            );

            Submission primaSottomissione = submissionService.addSubmission(subRequest1);

            System.out.println("SUCCESSO! Sottomissione creata nel Database.");
            System.out.println("ID Sottomissione: " + primaSottomissione.getId());
            System.out.println("URL Progetto: " + primaSottomissione.getProjectUrl());
            System.out.println("Data invio: " + primaSottomissione.getSubmissionDate());

            System.out.println("\n--- 7. Simulazione Aggiornamento Sottomissione ---");
            System.out.println("Il team ha trovato un bug e invia un nuovo URL prima della scadenza...");

            CreateSubmissionDTO subRequest2 = new CreateSubmissionDTO(
                    enrolledTeam.getId(),
                    createdHackathon.getId(),
                    "https://github.com/spring-booters/hackhub-v2-fixed",
                    "Versione corretta senza bug!",
                    null
            );

            Submission sottomissioneAggiornata = submissionService.addSubmission(subRequest2);

            System.out.println("SUCCESSO! Sottomissione aggiornata.");
            System.out.println("ID Sottomissione (dovrebbe essere lo stesso di prima): " + sottomissioneAggiornata.getId());
            System.out.println("Nuovo URL Progetto: " + sottomissioneAggiornata.getProjectUrl());
            System.out.println("Nuova Descrizione: " + sottomissioneAggiornata.getDescription());

        } catch (Exception e) {
            System.err.println("\nERRORE: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Forza l'uscita per chiudere i thread in background di Hibernate
            System.exit(0);
        }
    }
}