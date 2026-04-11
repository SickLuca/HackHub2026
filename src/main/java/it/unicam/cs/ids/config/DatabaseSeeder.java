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
        StaffUser organizer = createStaff("Alice", "Organizzatrice", "alice@hackhub.com", StaffRole.ORGANIZER);
        StaffUser judge = createStaff("Bob", "Giudice", "bob@hackhub.com", StaffRole.JUDGE);
        StaffUser mentor1 = createStaff("Carlo", "Mentore", "carlo@hackhub.com", StaffRole.MENTOR);
        StaffUser mentor2 = createStaff("Diana", "Mentore", "diana@hackhub.com", StaffRole.MENTOR);

        staffRepo.saveAll(List.of(organizer, judge, mentor1, mentor2));

        // 2. CREAZIONE UTENTI DEFAULT
        DefaultUser leader1 = createUser("Mario", "Rossi", "mario@mail.com", UserRole.TEAM_LEADER);
        DefaultUser member1 = createUser("Luigi", "Verdi", "luigi@mail.com", UserRole.TEAM_MEMBER);
        DefaultUser noTeamUser = createUser("Giovanna", "Bianchi", "giovanna@mail.com", UserRole.USER_NO_TEAM);

        userRepo.saveAll(List.of(leader1, member1, noTeamUser));

        // 3. CREAZIONE HACKATHON (usiamo il tuo fantastico Builder!)
        Hackathon hackathon = new ConcreteHackathonBuilder()
                .withName("Spring Boot Challenge 2026")
                .withStartDate(LocalDateTime.now().minusDays(2))
                .withRegistrationDeadline(LocalDateTime.now().minusDays(1))
                .withSubmitDeadline(LocalDateTime.now().plusDays(4))
                .withRegulation("Vietato copiare codice generato senza capirlo!")
                .withCashPrize(5000.0)
                .withLocation("Online")
                .withMaxDimensionOfTeam(4)
                .withOrganizer(organizer)
                .withJudge(judge)
                .withMentorsIds(List.of(mentor1, mentor2))
                .build();

        // Forziamo lo stato a IN_PROGRESS per testare sottomissioni e richieste
        hackathon.setStatus(HackathonStatus.IN_PROGRESS);
        hackathonRepo.save(hackathon);

        // 4. CREAZIONE TEAM E ASSEGNAZIONE
        Team teamAlpha = new Team();
        teamAlpha.setName("I Caffeinomani");
        teamAlpha.setSubscribedHackathon(hackathon);
        teamRepo.save(teamAlpha);

        // Aggiorniamo gli utenti con il team
        leader1.setTeam(teamAlpha);
        member1.setTeam(teamAlpha);
        userRepo.saveAll(List.of(leader1, member1));

        // 5. CREAZIONE INVITO
        Invitation invitation = new Invitation();
        invitation.setFromTeam(teamAlpha);
        invitation.setToUser(noTeamUser);
        invitation.setDescription("Ehi Giovanna, unisciti a noi per vincere!");
        invitation.setCreationDate(LocalDateTime.now());
        invitation.setStatus(InvitationStatus.PENDING);
        invitationRepo.save(invitation);

        // 6. CREAZIONE SOTTOMISSIONE
        Submission submission = new Submission();
        submission.setTeam(teamAlpha);
        submission.setHackathon(hackathon);
        submission.setProjectUrl("https://github.com/mario-rossi/spring-challenge");
        submission.setDescription("Questo è il nostro fantastico progetto.");
        submission.setSubmissionDate(LocalDateTime.now());
        submission.setStatus(SubmissionStatus.OPEN);
        submission.setScore(0);
        submission.setJudgeFeedback("");
        submissionRepo.save(submission);

        // 7. CREAZIONE RICHIESTA DI SUPPORTO
        SupportRequest supportReq = new SupportRequest();
        supportReq.setTeam(teamAlpha);
        supportReq.setHackathon(hackathon);
        supportReq.setMessage("Abbiamo un problema con la configurazione di Spring Security, un mentore può aiutarci?");
        supportReq.setStatus(SupportRequestStatus.PENDING);
        supportReq.setCreatedAt(LocalDateTime.now());
        supportRepo.save(supportReq);

        // 8. CREAZIONE SEGNALAZIONE (REPORT)
        Report report = new Report();
        report.setMentor(mentor1);
        report.setTeam(teamAlpha);
        report.setHackathon(hackathon);
        report.setDescription("Il team sta usando librerie non consentite dal regolamento.");
        report.setStatus(ReportStatus.PENDING);
        report.setCreatedAt(LocalDateTime.now());
        report.setDecisionNote("N/D");
        reportRepo.save(report);

        System.out.println("...Database popolato con successo!");
        System.out.println("ID staff: " + organizer.getId() + " " + judge.getId() + " " + mentor1.getId() + " " + mentor2.getId());
        System.out.println("ID utenti: " + leader1.getId() + " " + member1.getId() + " " + noTeamUser.getId());
        System.out.println("ID hackathon: " + hackathon.getId());
        System.out.println("ID team: " + teamAlpha.getId());
        System.out.println("ID invito: " + invitation.getId());
        System.out.println("ID sottomissione: " + submission.getId());
        System.out.println("ID richiesta di supporto: " + supportReq.getId());
        System.out.println("ID segnalazione: " + report.getId());
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