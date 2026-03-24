package it.unicam.cs.ids.validators;

import it.unicam.cs.ids.dtos.requests.CreateReportDTO;
import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.models.StaffUser;
import it.unicam.cs.ids.models.Team;
import it.unicam.cs.ids.utils.unitOfWork.IUnitOfWork;
import it.unicam.cs.ids.validators.abstractions.Validator;

public class CreateReportValidator implements Validator<CreateReportDTO> {

    private final IUnitOfWork unitOfWork;

    public CreateReportValidator(IUnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    @Override
    public void validate(CreateReportDTO request) {
        if (request.description() == null || request.description().trim().isEmpty()) {
            throw new IllegalArgumentException("La descrizione della segnalazione non può essere vuota.");
        }

        Hackathon hackathon = unitOfWork.getHackathonRepository().getById(request.hackathonId());
        if (hackathon == null) {
            throw new IllegalArgumentException("Hackathon non trovato.");
        }

        StaffUser mentor = unitOfWork.getStaffUserRepository().getById(request.mentorId());
        if (mentor == null) {
            throw new IllegalArgumentException("Mentore non trovato.");
        }

        Team team = unitOfWork.getTeamRepository().getById(request.teamId());
        if (team == null) {
            throw new IllegalArgumentException("Team non trovato.");
        }

        // Controllo se il team è iscritto a questo hackathon
        if (!team.getSubscribedHackathon().getId().equals(hackathon.getId())) {
            throw new IllegalArgumentException("Il team segnalato non partecipa a questo hackathon.");
        }

        // Controllo se il mentore è assegnato a questo hackathon
        boolean isMentorAssigned = hackathon.getMentors().stream()
                .anyMatch(m -> m.getId().equals(mentor.getId()));
        if (!isMentorAssigned) {
            throw new SecurityException("Il mentore non è assegnato a questo hackathon e non può effettuare segnalazioni.");
        }


    }
}