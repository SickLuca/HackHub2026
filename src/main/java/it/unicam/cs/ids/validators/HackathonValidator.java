package it.unicam.cs.ids.validators;

import it.unicam.cs.ids.dtos.CreateHackathonDTO;
import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.validators.abstractions.Validator;

public class HackathonValidator implements Validator<Hackathon> {
    @Override
    public boolean validate(Hackathon hackathon) {
        if(hackathon == null) return false;
        if(hackathon.getStartDate().isAfter(hackathon.getEndDate())) return false;
        if(hackathon.getMaxDimensionOfTeam() < 1) return false;
        if(hackathon.getCashPrize() < 0) return false;

        return true;
    }
}
