package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.requests.CreateSupportRequestDTO;
import it.unicam.cs.ids.dtos.responses.SupportRequestResponseDTO;
import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.models.SupportRequest;
import it.unicam.cs.ids.models.Team;
import it.unicam.cs.ids.models.utils.SupportRequestStatus;
import it.unicam.cs.ids.services.abstractions.ISupportRequestService;
import it.unicam.cs.ids.utils.unitOfWork.IUnitOfWork;
import it.unicam.cs.ids.validators.abstractions.Validator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class SupportRequestService implements ISupportRequestService {

    private final IUnitOfWork unitOfWork;
    private final Validator<CreateSupportRequestDTO> validator;


    public SupportRequestService(IUnitOfWork unitOfWork, Validator<CreateSupportRequestDTO> validator) {
        this.unitOfWork = unitOfWork;
        this.validator = validator;
    }

    @Override
    public SupportRequestResponseDTO createRequest(CreateSupportRequestDTO requestDTO) {
        validator.validate(requestDTO);

        // 1. Recupero entità associate
        Team team = unitOfWork.getTeamRepository().getById(requestDTO.teamId());
        if (team == null) throw new IllegalArgumentException("Team non trovato");

        Hackathon hackathon = unitOfWork.getHackathonRepository().getById(requestDTO.hackathonId());
        if (hackathon == null) throw new IllegalArgumentException("Hackathon non trovato");

        // (Opzionale) Controllare che il Team sia effettivamente iscritto a questo Hackathon
        if (!team.getSubscribedHackathon().getId().equals(hackathon.getId())) {
            throw new IllegalStateException("Il team non è iscritto a questo Hackathon");
        }

        boolean exists = unitOfWork.getSupportRequestRepository().getByHackathonId(requestDTO.hackathonId()).stream()
                .filter(request -> request.getStatus().equals(SupportRequestStatus.PENDING)) //Filtro le PENDING
                .anyMatch(request -> request.getTeam().getId().equals(team.getId())); //Vedo se esiste una richiesta a nome del team in questione

        if (!exists) {
            // 2. Creazione entità
            SupportRequest request = new SupportRequest();
            request.setTeam(team);
            request.setHackathon(hackathon);
            request.setMessage(requestDTO.message());
            request.setStatus(SupportRequestStatus.PENDING);
            request.setCreatedAt(LocalDateTime.now());

            // 3. Salvataggio
            unitOfWork.getSupportRequestRepository().create(request);

            // 4. Ritorno DTO
            return mapToDTO(request);
        } else throw new IllegalStateException("Esiste già una richiesta per questo team in questo Hackathon");
    }

    @Override
    public List<SupportRequestResponseDTO> getRequestsForHackathon(Long hackathonId, Long mentorId) {
        // 1. (Sicurezza) Verifichiamo che l'utente sia davvero un mentore per questo Hackathon
        Hackathon hackathon = unitOfWork.getHackathonRepository().getById(hackathonId);
        if (hackathon == null) throw new IllegalArgumentException("Hackathon non trovato");

        boolean isMentor = hackathon.getMentors().stream()
                .anyMatch(mentor -> mentor.getId().equals(mentorId));

        if (!isMentor) {
            throw new SecurityException("Non sei assegnato come mentore a questo Hackathon");
        }

        // 2. Recuperiamo le richieste
        List<SupportRequest> requests = unitOfWork.getSupportRequestRepository().getByHackathonId(hackathonId);

        // 3. Mappiamo a DTO
        return requests.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private SupportRequestResponseDTO mapToDTO(SupportRequest request) {
        return new SupportRequestResponseDTO(
                request.getId(),
                request.getTeam().getName(),
                request.getHackathon().getName(),
                request.getMessage(),
                request.getStatus().name(),
                request.getCreatedAt()
        );
    }
}