package it.unicam.cs.ids.utils.builder;

import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.models.StaffUser;
import it.unicam.cs.ids.models.utils.HackathonStatus;

import java.time.LocalDateTime;
import java.util.List;

public class ConcreteHackathonBuilder implements HackathonBuilder {
    private final Hackathon hackathon;

    public ConcreteHackathonBuilder() {
        this.hackathon = new Hackathon();
    }

    @Override
    public HackathonBuilder withName(String name) {
        hackathon.setName(name);
        return this;
    }

    @Override
    public HackathonBuilder withStartDate(LocalDateTime startDate) {
        hackathon.setStartDate(startDate);
        return this;
    }

    @Override
    public HackathonBuilder withEndDate(LocalDateTime endDate) {
        hackathon.setEndDate(endDate);
        return this;
    }

    @Override
    public HackathonBuilder withRegistrationDeadline(LocalDateTime registrationDeadline) {
        hackathon.setRegistrationDeadline(registrationDeadline);
        return this;
    }

    @Override
    public HackathonBuilder withSubmitDeadline(LocalDateTime submitDeadline) {
        hackathon.setSubmitDeadline(submitDeadline);
        return this;
    }

    @Override
    public HackathonBuilder withRegulation(String regulation) {
        hackathon.setRegulation(regulation);
        return this;
    }

    @Override
    public HackathonBuilder withCashPrize(Double cashPrize) {
        hackathon.setCashPrize(cashPrize);
        return this;
    }

    @Override
    public HackathonBuilder withLocation(String location) {
        hackathon.setLocation(location);
        return this;
    }

    @Override
    public HackathonBuilder withMaxDimensionOfTeam(Integer maxDimensionOfTeam) {
        hackathon.setMaxDimensionOfTeam(maxDimensionOfTeam);
        return this;
    }

    @Override
    public HackathonBuilder withStatus() {
        hackathon.setStatus(HackathonStatus.REGISTRATION);
        return this;
    }

    @Override
    public HackathonBuilder withOrganizer(StaffUser organizer) {
        hackathon.setOrganizer(organizer);
        return this;
    }

    @Override
    public HackathonBuilder withJudge(StaffUser judgeId) {
        hackathon.setJudge(judgeId);
        return this;
    }

    @Override
    public HackathonBuilder withMentorsIds(List<StaffUser> mentors) {
        hackathon.setMentors(mentors);
        return this;
    }

    @Override
    public Hackathon reset() {
        return new Hackathon();
    }

    @Override
    public Hackathon build() {
        return hackathon;
    }
}
