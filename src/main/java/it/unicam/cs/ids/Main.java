package it.unicam.cs.ids;

import it.unicam.cs.ids.dtos.CreateHackathonDTO;
import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.models.StaffUser;
import it.unicam.cs.ids.models.utils.StaffRole;
import it.unicam.cs.ids.repositories.HackathonRepository;
import it.unicam.cs.ids.repositories.StaffUserRepository;
import it.unicam.cs.ids.repositories.abstractions.IHackathonRepository;
import it.unicam.cs.ids.repositories.abstractions.IStaffUserRepository;
import it.unicam.cs.ids.services.HackathonService;
import it.unicam.cs.ids.services.abstractions.IHackathonService;
import it.unicam.cs.ids.validators.HackathonValidator;
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

        } catch (Exception e) {
            System.err.println("\nERRORE: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Forza l'uscita per chiudere i thread in background di Hibernate
            System.exit(0);
        }
    }
}