package it.unicam.cs.ids.dtos.requests;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequestDTO(
        @NotBlank(message = "Devi inserire un nome")
        String name,
        @NotBlank(message = "Devi inserire un cognome")
        String surname,
        @NotBlank(message = "Devi inserire un email")
        String email,
        @NotBlank(message = "Devi inserire una password")
        String password) {

}