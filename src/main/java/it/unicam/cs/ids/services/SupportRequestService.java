package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.requests.CreateSupportRequestDTO;
import it.unicam.cs.ids.dtos.requests.ScheduleCallDTO;
import it.unicam.cs.ids.dtos.responses.SupportRequestResponseDTO;
import it.unicam.cs.ids.exceptions.UnauthorizedActionException;
import it.unicam.cs.ids.models.*;
import it.unicam.cs.ids.models.utils.SupportRequestStatus;
import it.unicam.cs.ids.utils.adapter.ICalendarService;
import it.unicam.cs.ids.services.abstractions.ISupportRequestService;
import it.unicam.cs.ids.utils.unitOfWork.IUnitOfWork;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SupportRequestService implements ISupportRequestService {

    private final IUnitOfWork unitOfWork;
    private final ICalendarService calendarService;

    public SupportRequestService(IUnitOfWork unitOfWork, ICalendarService calendarService) {
        this.unitOfWork = unitOfWork;
        this.calendarService = calendarService;
    }

    @Override
    public SupportRequestResponseDTO createRequest(CreateSupportRequestDTO requestDTO, Long userId) {
        // 1. Recupero Utente e Team in modo sicuro dal token
        DefaultUser user = unitOfWork.getDefaultUserRepository().findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));

        Team team = user.getTeam();
        if (team == null) {
            throw new IllegalStateException("Devi appartenere a un team per poter richiedere supporto.");
        }

        Hackathon hackathon = unitOfWork.getHackathonRepository().findById(requestDTO.hackathonId()).orElse(null);
        if (hackathon == null) throw new IllegalArgumentException("Hackathon non trovato");

        // (Opzionale) Controllare che il Team sia effettivamente iscritto a questo Hackathon
        if (!team.getSubscribedHackathon().getId().equals(hackathon.getId())) {
            throw new IllegalStateException("Il team non è iscritto a questo Hackathon");
        }

        boolean exists = unitOfWork.getSupportRequestRepository().findByHackathonId(requestDTO.hackathonId()).stream()
                .filter(request -> request.getStatus().equals(SupportRequestStatus.PENDING)) //Filtro le PENDING
                .anyMatch(request -> request.getTeam().getId().equals(team.getId())); //Vedo se esiste una richiesta a nome del team in questione

        if (exists) {
            throw new IllegalStateException("Esiste già una richiesta di supporto in attesa per il tuo team in questo Hackathon");
        }

        // 2. Creazione entità
        SupportRequest request = new SupportRequest();
        request.setTeam(team);
        request.setHackathon(hackathon);
        request.setMessage(requestDTO.message());
        request.setStatus(SupportRequestStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());

        // 3. Salvataggio
        unitOfWork.getSupportRequestRepository().save(request);

        //aggiorno la relazione bidirezionale in memoria
        team.getSupportRequests().add(request);

        //aggiorno la relazione bidirezionale in memoria
        hackathon.getSupportRequests().add(request);

        // 4. Ritorno DTO
        return mapToDTO(request);

    }

    @Override
    public List<SupportRequestResponseDTO> getRequestsForHackathon(Long hackathonId, Long mentorId) {
        // 1. (Sicurezza) Verifichiamo che l'utente sia davvero un mentore per questo Hackathon
        Hackathon hackathon = unitOfWork.getHackathonRepository().findById(hackathonId).orElse(null);
        if (hackathon == null) throw new IllegalArgumentException("Hackathon non trovato");

        boolean isMentor = hackathon.getMentors().stream()
                .anyMatch(mentor -> mentor.getId().equals(mentorId));

        if (!isMentor) {
            throw new UnauthorizedActionException("Non sei assegnato come mentore a questo Hackathon");
        }

        // 2. Recuperiamo le richieste
        List<SupportRequest> requests = unitOfWork.getSupportRequestRepository().findByHackathonId(hackathonId);

        // 3. Mappiamo a DTO
        return requests.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    @Override
    public SupportRequestResponseDTO scheduleCall(ScheduleCallDTO request, Long mentorId) {
        // 1. Recupero la richiesta
        SupportRequest supportRequest = unitOfWork.getSupportRequestRepository().findById(request.supportRequestId())
                .orElseThrow(() -> new IllegalArgumentException("Richiesta di supporto non trovata"));


        // 2. Recupero il mentore
        StaffUser mentor = unitOfWork.getStaffUserRepository().findById(mentorId)
                .orElseThrow(() -> new IllegalArgumentException("Mentore non trovato"));

        // 3. Verifico che la richiesta sia in stato PENDING
        if (supportRequest.getStatus() != SupportRequestStatus.PENDING) {
            throw new IllegalStateException("Questa richiesta è già stata gestita o è chiusa.");
        }

        // 4. Verifico che il mentore sia assegnato all'hackathon di questa richiesta
        boolean isMentor = supportRequest.getHackathon().getMentors().stream()
                .anyMatch(m -> m.getId().equals(mentorId));

        if (!isMentor) {
            throw new UnauthorizedActionException("Non sei assegnato come mentore a questo Hackathon.");
        }

        // 5. Deleghiamo al sistema esterno la generazione del link
        String meetingLink = calendarService.generateMeetingLink(mentor.getName(), supportRequest.getTeam().getName());

        // 6. Aggiorniamo l'entità
        supportRequest.setStatus(SupportRequestStatus.SCHEDULED);
        supportRequest.setMeetingLink(meetingLink);
        supportRequest.setMeetingDate(request.callDate());

        unitOfWork.getSupportRequestRepository().save(supportRequest);

        return mapToDTO(supportRequest);
    }

    private SupportRequestResponseDTO mapToDTO(SupportRequest request) {
        return new SupportRequestResponseDTO(
                request.getId(),
                request.getTeam().getName(),
                request.getHackathon().getName(),
                request.getMessage(),
                request.getStatus().name(),
                request.getCreatedAt(),
                request.getMeetingLink() == null ? "Not Planned" : request.getMeetingLink(),
                request.getMeetingDate()
        );
    }
}