package it.unicam.cs.ids.dtos.responses;

import it.unicam.cs.ids.models.utils.HackathonStatus;

import java.time.LocalDateTime;
import java.util.List;

public record HackathonResponseDTO(
        Long id,
        String name,
        LocalDateTime startDate,
        LocalDateTime endDate,
        LocalDateTime registrationDeadline,
        LocalDateTime submitDeadline,
        String regulation,
        Double cashPrize,
        String location,
        Integer maxDimensionOfTeam,
        HackathonStatus status,
        String organizerName,  // Solo le info essenziali, non l'oggetto StaffUser
        String judgeName,
        List<String> mentorNames
) {
}
