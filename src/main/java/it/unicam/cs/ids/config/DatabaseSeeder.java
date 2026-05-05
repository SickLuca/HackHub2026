package it.unicam.cs.ids.config;

import it.unicam.cs.ids.models.*;
import it.unicam.cs.ids.models.utils.*;
import it.unicam.cs.ids.repositories.*;
import it.unicam.cs.ids.utils.builder.ConcreteHackathonBuilder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final IStaffUserRepository staffRepo;
    private final IDefaultUserRepository userRepo;
    private final IHackathonRepository hackathonRepo;
    private final ITeamRepository teamRepo;
    private final ISubmissionRepository submissionRepo;
    private final IReportRepository reportRepo;
    private final ISupportRequestRepository supportRepo;
    private final IInvitationRepository invitationRepo;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(IStaffUserRepository staffRepo, IDefaultUserRepository userRepo,
                          IHackathonRepository hackathonRepo, ITeamRepository teamRepo,
                          ISubmissionRepository submissionRepo, IReportRepository reportRepo,
                          ISupportRequestRepository supportRepo, IInvitationRepository invitationRepo,
                          PasswordEncoder passwordEncoder) {
        this.staffRepo = staffRepo;
        this.userRepo = userRepo;
        this.hackathonRepo = hackathonRepo;
        this.teamRepo = teamRepo;
        this.submissionRepo = submissionRepo;
        this.reportRepo = reportRepo;
        this.supportRepo = supportRepo;
        this.invitationRepo = invitationRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (staffRepo.count() > 0) {
            System.out.println("Database già popolato. Skip del seeding.");
            return;
        }

        System.out.println("Inizio popolamento del database con dati fittizi...");

        // 1. CREAZIONE STAFF
        StaffUser organizer1 = createStaff("Alice", "Organizzatrice", "alice@hackhub.com", StaffRole.ORGANIZER);
        StaffUser organizer2 = createStaff("Marco", "Ferrari", "marco@hackhub.com", StaffRole.ORGANIZER);
        StaffUser organizer3 = createStaff("Sara", "Conti", "sara@hackhub.com", StaffRole.ORGANIZER);

        StaffUser judge1 = createStaff("Bob", "Giudice", "bob@hackhub.com", StaffRole.JUDGE);
        StaffUser judge2 = createStaff("Elena", "Marino", "elena@hackhub.com", StaffRole.JUDGE);
        StaffUser judge3 = createStaff("Luca", "Ricci", "luca.judge@hackhub.com", StaffRole.JUDGE);

        StaffUser mentor1 = createStaff("Carlo", "Mentore", "carlo@hackhub.com", StaffRole.MENTOR);
        StaffUser mentor2 = createStaff("Diana", "Mentore", "diana@hackhub.com", StaffRole.MENTOR);
        StaffUser mentor3 = createStaff("Fabio", "Greco", "fabio@hackhub.com", StaffRole.MENTOR);
        StaffUser mentor4 = createStaff("Giulia", "Bruno", "giulia@hackhub.com", StaffRole.MENTOR);

        staffRepo.saveAll(List.of(organizer1, organizer2, organizer3,
                judge1, judge2, judge3,
                mentor1, mentor2, mentor3, mentor4));


        // 2. CREAZIONE UTENTI DEFAULT
        DefaultUser leader1 = createUser("Mario", "Rossi", "mario@mail.com", UserRole.TEAM_LEADER);
        DefaultUser leader2 = createUser("Anna", "Neri", "anna@mail.com", UserRole.TEAM_LEADER);
        DefaultUser leader3 = createUser("Paolo", "Galli", "paolo@mail.com", UserRole.TEAM_LEADER);

        DefaultUser member1 = createUser("Luigi", "Verdi", "luigi@mail.com", UserRole.TEAM_MEMBER);
        DefaultUser member2 = createUser("Chiara", "Esposito", "chiara@mail.com", UserRole.TEAM_MEMBER);
        DefaultUser member3 = createUser("Davide", "Romano", "davide@mail.com", UserRole.TEAM_MEMBER);
        DefaultUser member4 = createUser("Federica", "Costa", "federica@mail.com", UserRole.TEAM_MEMBER);
        DefaultUser member5 = createUser("Giorgio", "Mancini", "giorgio@mail.com", UserRole.TEAM_MEMBER);

        DefaultUser noTeam1 = createUser("Giovanna", "Bianchi", "giovanna@mail.com", UserRole.USER_NO_TEAM);
        DefaultUser noTeam2 = createUser("Roberto", "Lombardi", "roberto@mail.com", UserRole.USER_NO_TEAM);
        DefaultUser noTeam3 = createUser("Valentina", "Moretti", "valentina@mail.com", UserRole.USER_NO_TEAM);

        userRepo.saveAll(List.of(leader1, leader2, leader3,
                member1, member2, member3, member4, member5,
                noTeam1, noTeam2, noTeam3));

        // 3. CREAZIONE HACKATHON (usiamo il tuo fantastico Builder!)
        Hackathon hackathon1 = new ConcreteHackathonBuilder()
                .withName("Spring Boot Challenge 2026")
                .withStartDate(LocalDateTime.now().minusDays(2))
                .withRegistrationDeadline(LocalDateTime.now().minusDays(1))
                .withSubmitDeadline(LocalDateTime.now().plusDays(4))
                .withRegulation("Vietato copiare codice generato senza capirlo!")
                .withCashPrize(5000.0)
                .withLocation("Online")
                .withMaxDimensionOfTeam(4)
                .withOrganizer(organizer1)
                .withJudge(judge1)
                .withMentorsIds(List.of(mentor1, mentor2))
                .build();

        // Forziamo lo stato a IN_PROGRESS per testare sottomissioni e richieste
        hackathon1.setStatus(HackathonStatus.IN_PROGRESS);
        hackathonRepo.save(hackathon1);

        Hackathon hackathon2 = new ConcreteHackathonBuilder()
                .withName("AI Innovation Hackathon")
                .withStartDate(LocalDateTime.now().plusDays(10))
                .withRegistrationDeadline(LocalDateTime.now().plusDays(7))
                .withSubmitDeadline(LocalDateTime.now().plusDays(15))
                .withRegulation("Ogni team deve usare almeno un modello di machine learning.")
                .withCashPrize(10000.0)
                .withLocation("Milano")
                .withMaxDimensionOfTeam(5)
                .withOrganizer(organizer2)
                .withJudge(judge2)
                .withMentorsIds(List.of(mentor3, mentor4))
                .build();
        hackathon2.setStatus(HackathonStatus.REGISTRATION);

        Hackathon hackathon3 = new ConcreteHackathonBuilder()
                .withName("Cybersecurity CTF 2026")
                .withStartDate(LocalDateTime.now().minusDays(20))
                .withRegistrationDeadline(LocalDateTime.now().minusDays(25))
                .withSubmitDeadline(LocalDateTime.now().minusDays(5))
                .withRegulation("Nessun attacco a infrastrutture reali.")
                .withCashPrize(3000.0)
                .withLocation("Roma")
                .withMaxDimensionOfTeam(3)
                .withOrganizer(organizer3)
                .withJudge(judge3)
                .withMentorsIds(List.of(mentor1, mentor3))
                .build();

        hackathon3.setStatus(HackathonStatus.FINISHED);

        hackathonRepo.saveAll(List.of(hackathon2, hackathon3));

        // 4. CREAZIONE TEAM E ASSEGNAZIONE
        Team teamAlpha = new Team();
        teamAlpha.setName("I Caffeinomani");
        teamAlpha.setSubscribedHackathon(hackathon1);
        teamRepo.save(teamAlpha);

        Team teamBeta = new Team();
        teamBeta.setName("Neural Ninjas");
        teamBeta.setSubscribedHackathon(hackathon2);
        teamRepo.save(teamBeta);

        Team teamGamma = new Team();
        teamGamma.setName("Byte Busters");
        teamGamma.setSubscribedHackathon(hackathon3);
        teamRepo.save(teamGamma);

        // Aggiorniamo gli utenti con il team
        leader1.setTeam(teamAlpha);
        member1.setTeam(teamAlpha);
        member2.setTeam(teamAlpha);

        leader2.setTeam(teamBeta);
        member3.setTeam(teamBeta);
        member4.setTeam(teamBeta);

        leader3.setTeam(teamGamma);
        member5.setTeam(teamGamma);

        userRepo.saveAll(List.of(leader1, leader2, leader3,
                member1, member2, member3, member4, member5));


        List<Team> teams = List.of(teamAlpha, teamBeta);
        hackathon3.setTeams(teams);
        hackathon3.setWinner(teamAlpha);

        // 5. CREAZIONE INVITO
        Invitation inv1 = new Invitation();
        inv1.setFromTeam(teamAlpha);
        inv1.setToUser(noTeam1);
        inv1.setDescription("Ehi Giovanna, unisciti a noi per vincere!");
        inv1.setCreationDate(LocalDateTime.now());
        inv1.setStatus(InvitationStatus.PENDING);

        Invitation inv2 = new Invitation();
        inv2.setFromTeam(teamBeta);
        inv2.setToUser(noTeam2);
        inv2.setDescription("Roberto, abbiamo bisogno di te nel nostro team AI!");
        inv2.setCreationDate(LocalDateTime.now().minusDays(1));
        inv2.setStatus(InvitationStatus.ACCEPTED);

        Invitation inv3 = new Invitation();
        inv3.setFromTeam(teamGamma);
        inv3.setToUser(noTeam3);
        inv3.setDescription("Valentina, sei brava con la cyber security?");
        inv3.setCreationDate(LocalDateTime.now().minusDays(2));
        inv3.setStatus(InvitationStatus.REJECTED);

        invitationRepo.saveAll(List.of(inv1, inv2, inv3));

        // 6. CREAZIONE SOTTOMISSIONE
        Submission sub1 = new Submission();
        sub1.setTeam(teamAlpha);
        sub1.setHackathon(hackathon1);
        sub1.setProjectUrl("https://github.com/mario-rossi/spring-challenge");
        sub1.setDescription("Progetto fantastico con Spring Boot.");
        sub1.setSubmissionDate(LocalDateTime.now());
        sub1.setStatus(SubmissionStatus.OPEN);
        sub1.setScore(0);
        sub1.setJudgeFeedback("");

        Submission sub2 = new Submission();
        sub2.setTeam(teamGamma);
        sub2.setHackathon(hackathon3);
        sub2.setProjectUrl("https://github.com/paolo-galli/ctf-solution");
        sub2.setDescription("Soluzione CTF con tecniche avanzate.");
        sub2.setSubmissionDate(LocalDateTime.now().minusDays(6));
        sub2.setStatus(SubmissionStatus.EVALUATED);
        sub2.setScore(85);
        sub2.setJudgeFeedback("Ottimo lavoro, approccio creativo alla challenge.");

        submissionRepo.saveAll(List.of(sub1, sub2));

        // 7. CREAZIONE RICHIESTA DI SUPPORTO
        SupportRequest support1 = new SupportRequest();
        support1.setTeam(teamAlpha);
        support1.setHackathon(hackathon1);
        support1.setMessage("Abbiamo un problema con Spring Security, ci serve aiuto.");
        support1.setStatus(SupportRequestStatus.PENDING);
        support1.setCreatedAt(LocalDateTime.now());

        SupportRequest support2 = new SupportRequest();
        support2.setTeam(teamBeta);
        support2.setHackathon(hackathon2);
        support2.setMessage("Come integriamo un modello PyTorch con Spring Boot?");
        support2.setStatus(SupportRequestStatus.SCHEDULED);
        support2.setCreatedAt(LocalDateTime.now().minusHours(5));
        support2.setMeetingDate(LocalDateTime.now().plusDays(1));
        support2.setMeetingLink("https://meet.google.com/abc-defg-hij");

        supportRepo.saveAll(List.of(support1, support2));

        // 8. CREAZIONE SEGNALAZIONE (REPORT)
        Report report1 = new Report();
        report1.setMentor(mentor1);
        report1.setTeam(teamAlpha);
        report1.setHackathon(hackathon1);
        report1.setDescription("Il team sta usando librerie non consentite.");
        report1.setStatus(ReportStatus.PENDING);
        report1.setCreatedAt(LocalDateTime.now());
        report1.setDecisionNote("N/D");

        Report report2 = new Report();
        report2.setMentor(mentor3);
        report2.setTeam(teamGamma);
        report2.setHackathon(hackathon3);
        report2.setDescription("Sospetto utilizzo di exploit reali.");
        report2.setStatus(ReportStatus.RESOLVED);
        report2.setCreatedAt(LocalDateTime.now().minusDays(3));
        report2.setDecisionNote("Verificato, nessuna violazione reale commessa.");

        reportRepo.saveAll(List.of(report1, report2));

        System.out.println("...Database popolato con successo!");
/*
        System.out.println("ID staff: " + organizer1.getId() + " " + judge1.getId() + " " + mentor1.getId() + " " + mentor2.getId());
        System.out.println("ID utenti: " + leader1.getId() + " " + member1.getId() + " " + noTeam1.getId());
        System.out.println("ID hackathon: " + hackathon1.getId());
        System.out.println("ID team: " + teamAlpha.getId());
        System.out.println("ID invito: " + inv1.getId());
        System.out.println("ID sottomissione: " + sub1.getId());
        System.out.println("ID richiesta di supporto: " + support1.getId());
        System.out.println("ID segnalazione: " + report1.getId());

 */
    }

    // Metodi di utilità per mantenere il codice pulito
    private StaffUser createStaff(String name, String surname, String email, StaffRole role) {
        StaffUser user = new StaffUser();
        user.setName(name);
        user.setSurname(surname);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(role);
        return user;
    }

    private DefaultUser createUser(String name, String surname, String email, UserRole role) {
        DefaultUser user = new DefaultUser();
        user.setName(name);
        user.setSurname(surname);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(role);
        return user;
    }
}