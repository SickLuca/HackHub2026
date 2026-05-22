package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.requests.AddMentorDTO;
import it.unicam.cs.ids.dtos.requests.CreateHackathonDTO;
import it.unicam.cs.ids.dtos.requests.ProclaimWinnerDTO;
import it.unicam.cs.ids.dtos.responses.HackathonPublicResponseDTO;
import it.unicam.cs.ids.dtos.responses.HackathonResponseDTO;
import it.unicam.cs.ids.exceptions.InvalidInputException;
import it.unicam.cs.ids.exceptions.ResourceNotFoundException;
import it.unicam.cs.ids.exceptions.RuleViolationException;
import it.unicam.cs.ids.exceptions.UnauthorizedActionException;
import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.models.StaffUser;
import it.unicam.cs.ids.models.Team;
import it.unicam.cs.ids.models.utils.HackathonStatus;
import it.unicam.cs.ids.models.utils.StaffRole;
import it.unicam.cs.ids.models.utils.SubmissionStatus;
import it.unicam.cs.ids.services.abstractions.IHackathonService;
import it.unicam.cs.ids.utils.builder.ConcreteHackathonBuilder;
import it.unicam.cs.ids.utils.unitOfWork.IUnitOfWork;
import org.springframework.stereotype.Service;
import it.unicam.cs.ids.utils.strategy.PaymentProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing the lifecycle of Hackathons.
 * <p>
 * Implements the business logic for creating new hackathons
 * (using the Builder pattern), querying active events,
 * assigning mentors by organizers, and announcing
 * the winning team at the end of the competition.
 * </p>
 */
@Service
@Transactional
public class HackathonService implements IHackathonService {

    private final IUnitOfWork unitOfWork;
    private final PaymentProcessor paymentProcessor;

    public HackathonService(IUnitOfWork unitOfWork,
                            PaymentProcessor paymentProcessor) {
        this.unitOfWork = unitOfWork;
        this.paymentProcessor=paymentProcessor;
    }

    @Override
    public HackathonResponseDTO addHackathon(CreateHackathonDTO request, Long organizerId) {
        // 1. Retrieve the Organizer.
        StaffUser organizer = unitOfWork.getStaffUserRepository().findById(organizerId).orElse(null);
        if (organizer == null) {
            throw new ResourceNotFoundException("Organizer not found in the system");
        }
        if (organizer.getRole() != StaffRole.ORGANIZER) {
            throw new UnauthorizedActionException("Only organizers can create hackathons.");
        }

        // 2. Retrieve the Judge from the DB using the ID passed in the DTO
        StaffUser judge = unitOfWork.getStaffUserRepository().findById(request.judgeId()).orElse(null);
        if (judge == null) {
            throw new ResourceNotFoundException("Judge not found in the system.");
        }

        if(request.mentorsIdS().isEmpty()){
            throw new InvalidInputException("At least one mentor is required.");
        }
        // 3. Retrieve the Mentors from the DB
        List<StaffUser> mentors = new ArrayList<>();
        for (Long mentorId : request.mentorsIdS()) {
            StaffUser mentor = unitOfWork.getStaffUserRepository().findById(mentorId).orElse(null);
            if (mentor == null) {
                throw new ResourceNotFoundException("Mentor with ID " + mentorId + " not found in the system.");
            }
            mentors.add(mentor);
        }

        Hackathon hackathon = new ConcreteHackathonBuilder()
                .withName(request.name())
                .withStartDate(request.startDate())
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

        unitOfWork.getHackathonRepository().save(hackathon);

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
        List<Hackathon> hackathons = unitOfWork.getHackathonRepository().findAll();

        if (hackathons.isEmpty()) return null;

        List<HackathonResponseDTO> hackathonDTOs = new ArrayList<>();
        for (Hackathon h : hackathons) {
            hackathonDTOs.add(mapToDTO(h));
        }

        return hackathonDTOs;
    }

    @Override
    public HackathonResponseDTO addMentorToHackathon(AddMentorDTO request, Long organizerId) {

        // 1. Retrieve the Hackathon
        Hackathon hackathon = unitOfWork.getHackathonRepository().findById(request.hackathonId()).orElse(null);
        if (hackathon == null) {
            throw new ResourceNotFoundException("Hackathon not found");
        }

        if (hackathon.getStatus() == HackathonStatus.FINISHED || hackathon.getStatus() == HackathonStatus.UNDER_EVALUATION) {
            throw new InvalidInputException("You can no longer add mentors to this hackathon");
        }

        // 2. Security check: is the requester actually the organizer of this hackathon?
        if (!hackathon.getOrganizer().getId().equals(organizerId)){
            throw new UnauthorizedActionException("Only the organizer can add mentors to this hackathon");
        }

        // 3. Retrieve the Mentor to add
        StaffUser mentor = unitOfWork.getStaffUserRepository().findById(request.mentorId()).orElse(null);
        if (mentor == null) {
            throw new ResourceNotFoundException("Mentor not found in the system");
        }

        // 4. Duplicate check: is the mentor already assigned?
        boolean isAlreadyMentor = hackathon.getMentors().stream()
                .anyMatch(m -> m.getId().equals(mentor.getId()));

        if (isAlreadyMentor) {
            throw new InvalidInputException("This user is already a mentor for this hackathon");
        }

        // 5. Add and save
        hackathon.getMentors().add(mentor);
        unitOfWork.getHackathonRepository().save(hackathon);

        // 6. Return the updated DTO
        return mapToDTO(hackathon);
    }

  @Override
    public HackathonResponseDTO proclaimWinner(ProclaimWinnerDTO request, Long organizerId) {
        Hackathon hackathon = unitOfWork.getHackathonRepository().findById(request.hackathonId())
                .orElseThrow(() -> new ResourceNotFoundException("Hackathon not found"));

        // 1. Security check: only the organizer of THIS hackathon can do this
        if (!hackathon.getOrganizer().getId().equals(organizerId)) {
            throw new UnauthorizedActionException("Only the organizer can proclaim a winner.");
        }

        // 2. Status check: the hackathon must be under evaluation
        if (hackathon.getStatus() != HackathonStatus.UNDER_EVALUATION) {
            throw new RuleViolationException("Cannot proclaim a winner: the hackathon is not in the evaluation phase.");
        }

        boolean allEvaluated = hackathon.getSubmissions().stream()
                .allMatch(sub -> sub.getStatus() == SubmissionStatus.EVALUATED);

        if (!allEvaluated) {
            throw new RuleViolationException("Warning: not all submissions have been evaluated by the Judge.");
        }

        // 4. Retrieve the Winning Team and validate
        Team winner = unitOfWork.getTeamRepository().findById(request.winningTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Winning team not found"));

        if (!hackathon.getTeams().contains(winner)) {
            throw new ResourceNotFoundException("The selected team is not registered in this Hackathon.");
        }

        // 5. Proclaim and close
        hackathon.setWinner(winner);
        hackathon.setStatus(HackathonStatus.FINISHED);

        // 6. Award the prize using the strategy pattern
        if (hackathon.getCashPrize() != null && hackathon.getCashPrize() > 0) {
            paymentProcessor.processPayment(request.paymentMethod(), winner.getId(), hackathon.getCashPrize());
        }

        unitOfWork.getHackathonRepository().save(hackathon);

        return mapToDTO(hackathon);
    }


    @Override
    public List<HackathonPublicResponseDTO> getHackathonsPublicInfo() {
        return unitOfWork.getHackathonRepository().findAll().stream()
                .map(h -> new HackathonPublicResponseDTO(
                        h.getName(),
                        h.getStartDate(),
                        h.getStatus(),
                        h.getWinner() != null ? h.getWinner().getName() : "In progress/N.D."
                ))
                .toList();
    }

    private HackathonResponseDTO mapToDTO(Hackathon h) {
        return new HackathonResponseDTO(
                h.getId(),
                h.getName(),
                h.getStartDate(),
                h.getRegistrationDeadline(),
                h.getSubmitDeadline(),
                h.getRegulation(),
                h.getCashPrize(),
                h.getLocation(),
                h.getMaxDimensionOfTeam(),
                h.getStatus(),
                h.getOrganizer().getName() + " " + h.getOrganizer().getSurname(),
                h.getJudge().getName() + " " + h.getJudge().getSurname(),
                h.getMentors().stream().map(m -> m.getName() + " " + m.getSurname()).toList(),
                h.getWinner() == null ? "N/D" : h.getWinner().getName()
        );
    }
}