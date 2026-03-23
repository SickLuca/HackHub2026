package it.unicam.cs.ids.dtos.requests;

import java.time.LocalDateTime;
import java.util.List;

public record CreateHackathonDTO(
         Long organizerId,
         String name,
         LocalDateTime startDate,
         LocalDateTime endDate,
         LocalDateTime registrationDeadline,
         LocalDateTime submitDeadline,
         String regulation,
         Double cashPrize,
         String location,
         Integer maxDimensionOfTeam,
         Long judgeId,
         List<Long>mentorsIdS
) {
}
