package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.requests.CreateInvitationDTO;
import it.unicam.cs.ids.dtos.responses.InvitationResponseDTO;
import it.unicam.cs.ids.dtos.requests.RespondInvitationDTO;
import it.unicam.cs.ids.exceptions.InvalidInputException;
import it.unicam.cs.ids.exceptions.ResourceNotFoundException;
import it.unicam.cs.ids.exceptions.RuleViolationException;
import it.unicam.cs.ids.models.DefaultUser;
import it.unicam.cs.ids.models.Invitation;
import it.unicam.cs.ids.models.Team;
import it.unicam.cs.ids.models.utils.InvitationStatus;
import it.unicam.cs.ids.models.utils.UserRole;
import it.unicam.cs.ids.services.abstractions.IInvitationService;
import it.unicam.cs.ids.utils.unitOfWork.IUnitOfWork;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class InvitationService implements IInvitationService {

    private final IUnitOfWork unitOfWork;

    public InvitationService(IUnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    @Override
    public InvitationResponseDTO sendInvitation(CreateInvitationDTO request, Long fromTeamLeaderId) {

        DefaultUser inviter = unitOfWork.getDefaultUserRepository().findById(fromTeamLeaderId).orElse(null);
        // Controllo validità ruolo e appartenenza al team
        if (inviter.getRole() != UserRole.TEAM_LEADER) {
            throw new RuleViolationException("Solo un Team Leader può invitare nuovi membri nel team.");
        }

        Team team = inviter.getTeam();
        // Controllo capienza team in base all'hackathon (se iscritti)
        if (team.getSubscribedHackathon() != null) {
            if (team.getMembers().size() >= team.getSubscribedHackathon().getMaxDimensionOfTeam()) {
                throw new RuleViolationException("Il team ha già raggiunto la dimensione massima per l'hackathon a cui è iscritto.");
            }
        }

        DefaultUser invitedUser = unitOfWork.getDefaultUserRepository().findById(request.toUserId()).orElse(null);
        if (invitedUser == null) {
            throw new ResourceNotFoundException("Utente con id " + request.toUserId() + " non trovato");
        }


        // Controllo stato dell'utente invitato
        if (invitedUser.getRole() != UserRole.USER_NO_TEAM) {
            throw new RuleViolationException("L'utente invitato appartiene già a un team.");
        }


        // Creazione dell'entità
        Invitation invitation = new Invitation();
        invitation.setDescription(request.description());
        invitation.setFromTeam(team);
        invitation.setToUser(invitedUser);
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setCreationDate(LocalDateTime.now());

        Invitation savedInvitation = unitOfWork.getInvitationRepository().save(invitation);

        //Aggiornamento utente in memoria
        invitedUser.getInvitations().add(savedInvitation);

        //Aggiornamento utente sul db (non dovrebbe essere necessario per via di JPA che dopo il setToUser dovrebbe aver aggiornato le relazioni
        unitOfWork.getDefaultUserRepository().save(invitedUser);


        //aggiorno relazione bidirezionale in memoria
        team.getInvitations().add(savedInvitation);

        return mapToDTO(savedInvitation);
    }

    public List<InvitationResponseDTO> getAllInvitationsByCurrentUser(Long userId){

        if(userId == null){
            throw new InvalidInputException("User id is null");
        }

        List<InvitationResponseDTO> invitations = new ArrayList<>();

        unitOfWork.getInvitationRepository().findAll().stream()
                .filter(invitation -> invitation.getToUser().getId().equals(userId))
                .forEach(invitation -> invitations.add(mapToDTO(invitation)));

        return invitations;

    }

    @Override
    public InvitationResponseDTO respondToInvitation(RespondInvitationDTO request, Long userId){
        Invitation invitation = unitOfWork.getInvitationRepository().findById(request.invitationId()).orElse(null);
        if (invitation == null) {
            throw new ResourceNotFoundException("Invito non trovato.");
        }

        // 2. Verifico che l'invito sia ancora pendente
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new RuleViolationException("Questo invito è già stato gestito (accettato o rifiutato).");
        }

        // 3. Verifico l'identità dell'utente
        DefaultUser user = unitOfWork.getDefaultUserRepository().findById(invitation.getToUser().getId()).orElse(null);
        if (user == null || !invitation.getToUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("L'utente non è il destinatario di questo invito.");
        }

        if (request.accept()) {
            // 4. Controlliamo di nuovo che l'utente non abbia già un team (magari ha accettato un altro invito 5 minuti fa)
            if (user.getRole() != UserRole.USER_NO_TEAM) {
                throw new RuleViolationException("Impossibile accettare: appartieni già a un team.");
            }

            // 5. Controlliamo se il team è attualmente iscritto ad un hackathon
            Team team = invitation.getFromTeam();
            if (team.getSubscribedHackathon() != null) {
                throw new RuleViolationException("Impossibile accettare: il team sta partecipando ad un hackathon");
            }

            invitation.setStatus(InvitationStatus.ACCEPTED);

            // Aggiorno l'utente
            user.setTeam(team);
            user.setRole(UserRole.TEAM_MEMBER); // Da utente semplice diventa membro

            // Sincronizzo la relazione bidirezionale in memoria (il database verrebbe salvato comunque dal merge del team)
            team.getMembers().add(user);

            unitOfWork.getTeamRepository().save(team);
            unitOfWork.getDefaultUserRepository().save(user);
        } else {
            invitation.setStatus(InvitationStatus.REJECTED);
        }

        return mapToDTO(unitOfWork.getInvitationRepository().save(invitation));
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