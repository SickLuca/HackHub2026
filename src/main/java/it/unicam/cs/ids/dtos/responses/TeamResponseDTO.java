package it.unicam.cs.ids.dtos.responses;

import java.util.List;

public record TeamResponseDTO(
        Long id,
        String name,
        List<String> membersName,
        String subscribedHackathonName
) {
}
