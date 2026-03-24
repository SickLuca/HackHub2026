package it.unicam.cs.ids.dtos.requests;

public record AddMentorDTO(
        Long organizerId,
        Long hackathonId,
        Long mentorId
) {
}