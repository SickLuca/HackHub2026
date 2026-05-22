package it.unicam.cs.ids.dtos.requests;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequestDTO(
        @NotBlank(message = "You must provide a name")
        String name,
        @NotBlank(message = "You must provide a surname")
        String surname,
        @NotBlank(message = "You must provide an email")
        String email,
        @NotBlank(message = "You must provide a password")
        String password) {

}