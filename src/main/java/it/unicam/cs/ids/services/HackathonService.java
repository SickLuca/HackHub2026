package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.CreateHackathonDTO;
import it.unicam.cs.ids.dtos.HackathonResponseDTO;
import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.models.StaffUser;
import it.unicam.cs.ids.repositories.abstractions.IHackathonRepository;
import it.unicam.cs.ids.repositories.abstractions.IStaffUserRepository;
import it.unicam.cs.ids.services.abstractions.IHackathonService;
import it.unicam.cs.ids.utils.builder.ConcreteHackathonBuilder;
import it.unicam.cs.ids.utils.unitOfWork.IUnitOfWork;
import it.unicam.cs.ids.validators.abstractions.Validator;

import java.util.ArrayList;
import java.util.List;

public class HackathonService implements IHackathonService {

    private final IUnitOfWork unitOfWork;
    private final Validator<CreateHackathonDTO> hackathonValidator;

    public HackathonService(IUnitOfWork unitOfWork, Validator<CreateHackathonDTO> hackathonValidator) {
        this.unitOfWork = unitOfWork;
        this.hackathonValidator = hackathonValidator;
    }

    @Override
    public Hackathon addHackathon(CreateHackathonDTO request) {
        hackathonValidator.validate(request);

        // 1. Recupero l'Organizzatore.
        // TODO: In futuro questo ID arriverà dal token di autenticazione di chi fa la richiesta.
        StaffUser organizer = unitOfWork.getStaffUserRepository().getById(request.organizerId());
        if (organizer == null) {
            throw new IllegalArgumentException("Organizer not found in the system");
        }

        // 2. Recupero il Giudice dal DB usando l'ID passato nel DTO
        StaffUser judge = unitOfWork.getStaffUserRepository().getById(request.judgeId());
        if (judge == null) {
            throw new IllegalArgumentException("Judge not found in the system.");
        }

        // 3. Recupero i Mentori dal DB
        List<StaffUser> mentors = new ArrayList<>();
        for (Long mentorId : request.mentorsIdS()) {
            StaffUser mentor = unitOfWork.getStaffUserRepository().getById(mentorId);
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

        return unitOfWork.getHackathonRepository().create(hackathon);
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
    public List<HackathonResponseDTO> getAllHackathons() {
        List<Hackathon> hackathons = unitOfWork.getHackathonRepository().getAll();

        if (hackathons.isEmpty()) return null;

        return hackathons.stream()
                .map(h -> new HackathonResponseDTO(
                        h.getId(),
                        h.getName(),
                        h.getStartDate(),
                        h.getEndDate(),
                        h.getRegistrationDeadline(),
                        h.getSubmitDeadline(),
                        h.getRegulation(),
                        h.getCashPrize(),
                        h.getLocation(),
                        h.getMaxDimensionOfTeam(),
                        h.getStatus(),
                        h.getOrganizer().getName() + " " + h.getOrganizer().getSurname(),
                        h.getJudge().getName() + " " + h.getJudge().getSurname(),
                        h.getMentors().stream().map(m -> m.getName() + " " + m.getSurname()).toList()
                ))
                .toList();
    }
}
