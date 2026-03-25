package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.requests.CreateInvitationDTO;
import it.unicam.cs.ids.dtos.responses.InvitationResponseDTO;
import it.unicam.cs.ids.dtos.requests.RespondInvitationDTO;
import it.unicam.cs.ids.models.DefaultUser;
import it.unicam.cs.ids.models.Invitation;
import it.unicam.cs.ids.models.Team;
import it.unicam.cs.ids.models.utils.InvitationStatus;
import it.unicam.cs.ids.models.utils.UserRole;
import it.unicam.cs.ids.services.abstractions.IInvitationService;
import it.unicam.cs.ids.utils.unitOfWork.IUnitOfWork;
import it.unicam.cs.ids.validators.abstractions.Validator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InvitationService implements IInvitationService {

    private final IUnitOfWork unitOfWork;
    private final Validator<CreateInvitationDTO> validator;

    public InvitationService(IUnitOfWork unitOfWork, Validator<CreateInvitationDTO> validator) {
        this.unitOfWork = unitOfWork;
        this.validator = validator;
    }

    @Override
    public InvitationResponseDTO sendInvitation(CreateInvitationDTO request) {
        //Validazione sintattica del DTO
        validator.validate(request);

        Team team = unitOfWork.getTeamRepository().getById(request.fromTeamId());
        if (team == null) {
            throw new IllegalArgumentException("Team con id " + request.fromTeamId() + " non trovato");
        }

        DefaultUser inviter = unitOfWork.getDefaultUserRepository().getById(request.fromTeamLeaderId());
        if (inviter == null) {
            throw new IllegalArgumentException("Utente invitante con id " + request.fromTeamLeaderId() + " non trovato");
        }

        DefaultUser invitedUser = unitOfWork.getDefaultUserRepository().getById(request.toUserId());
        if (invitedUser == null) {
            throw new IllegalArgumentException("Utente con id " + request.toUserId() + " non trovato");
        }

        // Controllo validità ruolo e appartenenza al team
        if (inviter.getRole() != UserRole.TEAM_LEADER || !inviter.getTeam().getId().equals(team.getId())) {
            throw new IllegalStateException("Solo il Team Leader può invitare nuovi membri nel team.");
        }

        // Controllo stato dell'utente invitato
        if (invitedUser.getRole() != UserRole.USER_NO_TEAM) {
            throw new IllegalStateException("L'utente invitato appartiene già a un team.");
        }

        // Controllo capienza team in base all'hackathon (se iscritti)
        if (team.getSubscribedHackathon() != null) {
            if (team.getMembers().size() >= team.getSubscribedHackathon().getMaxDimensionOfTeam()) {
                throw new IllegalStateException("Il team ha già raggiunto la dimensione massima per l'hackathon a cui è iscritto.");
            }
        }

        // Creazione dell'entità
        Invitation invitation = new Invitation();
        invitation.setDescription(request.description());
        invitation.setFromTeam(team);
        invitation.setToUser(invitedUser);
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setCreationDate(LocalDateTime.now());

        Invitation savedInvitation = unitOfWork.getInvitationRepository().create(invitation);

        //Aggiornamento utente in memoria
        invitedUser.getInvitations().add(savedInvitation);

        //Aggiornamento utente sul db (non dovrebbe essere necessario per via di JPA che dopo il setToUser dovrebbe aver aggiornato le relazioni
        unitOfWork.getDefaultUserRepository().update(invitedUser);

        //aggiorno relazione bidirezionale in memoria
        team.getInvitations().add(savedInvitation);

        return mapToDTO(savedInvitation);
    }

    public List<InvitationResponseDTO> getAllInvitationsByUserId(Long userId){

        if(userId == null){
            throw new IllegalArgumentException("User id is null");
        }

        List<InvitationResponseDTO> invitations = new ArrayList<>();

        unitOfWork.getInvitationRepository().getAll().stream()
                .filter(invitation -> invitation.getToUser().getId().equals(userId))
                .forEach(invitation -> invitations.add(mapToDTO(invitation)));

        return invitations;

    }

    @Override
    public InvitationResponseDTO respondToInvitation(RespondInvitationDTO request){
        Invitation invitation = unitOfWork.getInvitationRepository().getById(request.invitationId());
        if (invitation == null) {
            throw new IllegalArgumentException("Invito non trovato.");
        }

        // 2. Verifico che l'invito sia ancora pendente
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("Questo invito è già stato gestito (accettato o rifiutato).");
        }

        // 3. Verifico l'identità dell'utente
        DefaultUser user = unitOfWork.getDefaultUserRepository().getById(invitation.getToUser().getId());
        if (user == null || !invitation.getToUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("L'utente non è il destinatario di questo invito.");
        }

        if (request.accept()) {
            // 4. Controlliamo di nuovo che l'utente non abbia già un team (magari ha accettato un altro invito 5 minuti fa)
            if (user.getRole() != UserRole.USER_NO_TEAM) {
                throw new IllegalStateException("Impossibile accettare: appartieni già a un team.");
            }

            // 5. Controlliamo se il team è attualmente iscritto ad un hackathon
            Team team = invitation.getFromTeam();
            if (team.getSubscribedHackathon() != null) {
                throw new IllegalStateException("Impossibile accettare: il team sta partecipando ad un hackathon");
            }

            invitation.setStatus(InvitationStatus.ACCEPTED);

            // Aggiorno l'utente
            user.setTeam(team);
            user.setRole(UserRole.TEAM_MEMBER); // Da utente semplice diventa membro

            // Sincronizzo la relazione bidirezionale in memoria (il database verrebbe salvato comunque dal merge del team)
            team.getMembers().add(user);

            unitOfWork.getTeamRepository().update(team);
            unitOfWork.getDefaultUserRepository().update(user);
        } else {
            invitation.setStatus(InvitationStatus.REJECTED);
        }

        return mapToDTO(unitOfWork.getInvitationRepository().update(invitation));
    }


    private InvitationResponseDTO mapToDTO(Invitation invitation) {
        return new InvitationResponseDTO(
                invitation.getToUser().getName(),
                invitation.getFromTeam().getName(),
                invitation.getStatus(),
                invitation.getCreationDate()
        );
    }


}
