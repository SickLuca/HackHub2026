package it.unicam.cs.ids.utils;

import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.models.StaffUser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface HackathonBuilder {

    HackathonBuilder withName(String name);
    HackathonBuilder withStartDate(LocalDateTime startDate);
    HackathonBuilder withEndDate(LocalDateTime endDate);
    HackathonBuilder withRegistrationDeadline(LocalDateTime registrationDeadline);
    HackathonBuilder withSubmitDeadline(LocalDateTime submitDeadline);
    HackathonBuilder withRegulation(String regulation);
    HackathonBuilder withCashPrize(Double cashPrize);
    HackathonBuilder withLocation(String location);
    HackathonBuilder withMaxDimensionOfTeam(Integer maxDimensionOfTeam);
    HackathonBuilder withStatus();
    HackathonBuilder withOrganizer(StaffUser organizer);
    HackathonBuilder withJudge(StaffUser judge);
    HackathonBuilder withMentorsIds(List<StaffUser> mentorsId);

    Hackathon reset();
    Hackathon build();

}
