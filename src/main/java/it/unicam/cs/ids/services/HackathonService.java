package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.requests.AddMentorDTO;
import it.unicam.cs.ids.dtos.requests.CreateHackathonDTO;
import it.unicam.cs.ids.dtos.responses.HackathonResponseDTO;
import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.models.StaffUser;
import it.unicam.cs.ids.models.utils.HackathonStatus;
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
    public HackathonResponseDTO addHackathon(CreateHackathonDTO request) {
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

        unitOfWork.getHackathonRepository().create(hackathon);

        return mapToDTO(hackathon);
    }

    @Override
    public HackathonResponseDTO updateHackathon(Hackathon hackathon) {
        return null;
    }

    @Override
    public HackathonResponseDTO deleteHackathon(Long id) {
        return null;
    }

    @Override
    public HackathonResponseDTO getHackathonById(Long id) {
        return null;
    }

    @Override
    public List<HackathonResponseDTO> getAllHackathons() {
        List<Hackathon> hackathons = unitOfWork.getHackathonRepository().getAll();

        if (hackathons.isEmpty()) return null;

        List<HackathonResponseDTO> hackathonDTOs = new ArrayList<>();
        for (Hackathon h : hackathons) {
            hackathonDTOs.add(mapToDTO(h));
        }

        return hackathonDTOs;
    }

    @Override
    public HackathonResponseDTO addMentorToHackathon(AddMentorDTO request) {
        // 1. Recupero l'Hackathon
        Hackathon hackathon = unitOfWork.getHackathonRepository().getById(request.hackathonId());
        if (hackathon == null) {
            throw new IllegalArgumentException("Hackathon non trovato");
        }

        if (hackathon.getStatus() == HackathonStatus.FINISHED || hackathon.getStatus() == HackathonStatus.UNDER_EVALUATION) {
            throw new IllegalStateException("Non puoi più aggiungere mentori a questo hackathon");
        }

        // 2. Controllo di sicurezza: chi fa la richiesta è davvero l'organizzatore di questo hackathon?
        if (!hackathon.getOrganizer().getId().equals(request.organizerId())) {
            throw new SecurityException("Solo l'organizzatore può aggiungere mentori a questo hackathon");
        }

        // 3. Recupero il Mentore da aggiungere
        StaffUser mentor = unitOfWork.getStaffUserRepository().getById(request.mentorId());
        if (mentor == null) {
            throw new IllegalArgumentException("Mentore non trovato nel sistema");
        }

        // 4. Controllo duplicati: il mentore è già assegnato?
        boolean isAlreadyMentor = hackathon.getMentors().stream()
                .anyMatch(m -> m.getId().equals(mentor.getId()));

        if (isAlreadyMentor) {
            throw new IllegalStateException("Questo utente è già un mentore per questo hackathon");
        }

        // 5. Aggiunta e salvataggio
        hackathon.getMentors().add(mentor);
        unitOfWork.getHackathonRepository().update(hackathon);

        // 6. Ritorno il DTO aggiornato
        return mapToDTO(hackathon);
    }


    private HackathonResponseDTO mapToDTO(Hackathon h) {
        return new HackathonResponseDTO(
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

        );
    }
}