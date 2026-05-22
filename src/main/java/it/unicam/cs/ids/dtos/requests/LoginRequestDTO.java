package it.unicam.cs.ids.dtos.requests;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank(message = "You must provide an email")
        String email,
        @NotBlank(message = "You must provide a password")
        String password) {}