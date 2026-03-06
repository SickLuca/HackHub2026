package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.CreateHackathonDTO;
import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.models.StaffUser;
import it.unicam.cs.ids.repositories.abstractions.IHackathonRepository;
import it.unicam.cs.ids.repositories.abstractions.IStaffUserRepository;
import it.unicam.cs.ids.services.abstractions.IHackathonService;
import it.unicam.cs.ids.utils.ConcreteHackathonBuilder;
import it.unicam.cs.ids.validators.abstractions.Validator;

import java.util.ArrayList;
import java.util.List;

public class HackathonService implements IHackathonService {

    private final IHackathonRepository hackathonRepository;
    // Aggiungiamo il repository per recuperare gli utenti dal DB!
    private final IStaffUserRepository staffUserRepository;

    private final Validator<Hackathon> hackathonValidator;

    public HackathonService(IHackathonRepository hackathonRepository, IStaffUserRepository staffUserRepository, Validator<Hackathon> hackathonValidator) {
        this.hackathonRepository = hackathonRepository;
        this.hackathonValidator = hackathonValidator;
        this.staffUserRepository = staffUserRepository;
    }

    @Override
    public Hackathon addHackathon(CreateHackathonDTO request) {

        // 1. Recupero l'Organizzatore.
        // TODO: In futuro questo ID arriverà dal token di autenticazione di chi fa la richiesta.
        StaffUser organizer = staffUserRepository.getById(request.organizerId());
        if (organizer == null) {
            throw new IllegalArgumentException("Organizer not found in the system");
        }

        // 2. Recupero il Giudice dal DB usando l'ID passato nel DTO
        StaffUser judge = staffUserRepository.getById(request.judgeId());
        if (judge == null) {
            throw new IllegalArgumentException("Judge not found in the system.");
        }

        // 3. Recupero i Mentori dal DB
        List<StaffUser> mentors = new ArrayList<>();
        for (Long mentorId : request.mentorsIdS()) {
            StaffUser mentor = staffUserRepository.getById(mentorId);
            if (mentor == null) {
                throw new IllegalArgumentException("Mentor with ID " + mentorId + " not found in the system.");
            }
            mentors.add(mentor);
        }

        Hackathon hackathon = new ConcreteHackathonBuilder()
                .withName(request.name())
                .withStartDate(request.startDate())
                .withEndDate(request.endDate())
                .withRegistrationDeadline(request.registrationDeadline())
                .withSubmitDeadline(request.submitDeadline())
                .withRegulation(request.regulation())
                .withCashPrize(request.cashPrize())
                .withLocation(request.location())
                .withMaxDimensionOfTeam(request.maxDimensionOfTeam())
                .withStatus()
                .withOrganizer(organizer)
                .withJudge(judge)
                .withMentorsIds(mentors)

                .build();

        if (hackathonValidator.validate(hackathon)) {
            return hackathonRepository.create(hackathon);
        } else {
            throw new IllegalArgumentException("Hackathon creation failed: invalid data");
        }
    }

    @Override
    public Hackathon updateHackathon(Hackathon hackathon) {
        return null;
    }

    @Override
    public Hackathon deleteHackathon(Long id) {
        return null;
    }

    @Override
    public Hackathon getHackathonById(Long id) {
        return null;
    }

    @Override
    public List<Hackathon> getAllHackathon() {
        return List.of();
    }
}
