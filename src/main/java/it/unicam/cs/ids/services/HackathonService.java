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
        // 1. Recupero l'Organizzatore.
        StaffUser organizer = unitOfWork.getStaffUserRepository().findById(organizerId).orElse(null);
        if (organizer == null) {
            throw new ResourceNotFoundException("Organizer not found in the system");
        }
        if (organizer.getRole() != StaffRole.ORGANIZER) {
            throw new UnauthorizedActionException("Only organizers can create hackathons.");
        }

        // 2. Recupero il Giudice dal DB usando l'ID passato nel DTO
        StaffUser judge = unitOfWork.getStaffUserRepository().findById(request.judgeId()).orElse(null);
        if (judge == null) {
            throw new ResourceNotFoundException("Judge not found in the system.");
        }

        if(request.mentorsIdS().isEmpty()){
            throw new InvalidInputException("At least one mentor is required.");
        }
        // 3. Recupero i Mentori dal DB
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

        // 1. Recupero l'Hackathon
        Hackathon hackathon = unitOfWork.getHackathonRepository().findById(request.hackathonId()).orElse(null);
        if (hackathon == null) {
            throw new ResourceNotFoundException("Hackathon non trovato");
        }

        if (hackathon.getStatus() == HackathonStatus.FINISHED || hackathon.getStatus() == HackathonStatus.UNDER_EVALUATION) {
            throw new InvalidInputException("Non puoi più aggiungere mentori a questo hackathon");
        }

        // 2. Controllo di sicurezza: chi fa la richiesta è davvero l'organizzatore di questo hackathon?
        if (!hackathon.getOrganizer().getId().equals(organizerId)){
            throw new UnauthorizedActionException("Solo l'organizzatore può aggiungere mentori a questo hackathon");
        }

        // 3. Recupero il Mentore da aggiungere
        StaffUser mentor = unitOfWork.getStaffUserRepository().findById(request.mentorId()).orElse(null);
        if (mentor == null) {
            throw new ResourceNotFoundException("Mentore non trovato nel sistema");
        }

        // 4. Controllo duplicati: il mentore è già assegnato?
        boolean isAlreadyMentor = hackathon.getMentors().stream()
                .anyMatch(m -> m.getId().equals(mentor.getId()));

        if (isAlreadyMentor) {
            throw new InvalidInputException("Questo utente è già un mentore per questo hackathon");
        }

        // 5. Aggiunta e salvataggio
        hackathon.getMentors().add(mentor);
        unitOfWork.getHackathonRepository().save(hackathon);

        // 6. Ritorno il DTO aggiornato
        return mapToDTO(hackathon);
    }

  @Override
    public HackathonResponseDTO proclaimWinner(ProclaimWinnerDTO request, Long organizerId) {
        Hackathon hackathon = unitOfWork.getHackathonRepository().findById(request.hackathonId())
                .orElseThrow(() -> new ResourceNotFoundException("Hackathon non trovato"));

        // 1. Controllo di Sicurezza: Solo l'organizzatore di QUESTO hackathon può farlo
        if (!hackathon.getOrganizer().getId().equals(organizerId)) {
            throw new UnauthorizedActionException("Solo l'organizzatore assegnato può proclamare il vincitore.");
        }

        // 2. Controllo di Stato: L'hackathon deve essere in valutazione
        if (hackathon.getStatus() != HackathonStatus.UNDER_EVALUATION) {
            throw new RuleViolationException("Impossibile proclamare un vincitore: l'hackathon non è in fase di valutazione.");
        }

        boolean allEvaluated = hackathon.getSubmissions().stream()
                .allMatch(sub -> sub.getStatus() == SubmissionStatus.EVALUATED);

        if (!allEvaluated) {
            throw new RuleViolationException("Attenzione: non tutte le sottomissioni sono state valutate dal Giudice.");
        }

        // 4. Recupero del Team Vincitore e controllo validità
        Team winner = unitOfWork.getTeamRepository().findById(request.winningTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Team vincitore non trovato"));

        if (!hackathon.getTeams().contains(winner)) {
            throw new ResourceNotFoundException("Il team selezionato non è iscritto a questo Hackathon.");
        }

        // 5. Proclamazione e Chiusura
        hackathon.setWinner(winner);
        hackathon.setStatus(HackathonStatus.FINISHED);

        // 6. Erogazione del premio con strategy pattern
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