package it.unicam.cs.ids.validators;

import it.unicam.cs.ids.models.Team;
import it.unicam.cs.ids.validators.abstractions.Validator;

public class TeamValidator implements Validator<Team> {
    @Override
    public boolean validate(Team team) {
        if(team == null) return false;
        if (team.getName().length() < 5) return false;
        return true;
    }
}