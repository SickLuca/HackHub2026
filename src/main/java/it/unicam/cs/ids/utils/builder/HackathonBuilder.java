package it.unicam.cs.ids.utils.builder;

import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.models.StaffUser;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interfaccia Builder per la creazione di istanze di {@link Hackathon}.
 * <p>
 * Fornisce un'interfaccia fluida per impostare in modo incrementale
 * i vari parametri complessi di un hackathon, prima di invocarne 
 * la costruzione finale.
 * </p>
 */
public interface HackathonBuilder {

    HackathonBuilder withName(String name);
    HackathonBuilder withStartDate(LocalDateTime startDate);
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
