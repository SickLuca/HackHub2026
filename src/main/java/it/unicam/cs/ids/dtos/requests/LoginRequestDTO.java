package it.unicam.cs.ids.dtos.requests;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank(message = "Devi inserire un email")
        String email,
        @NotBlank(message = "Devi inserire una password")
        String password) {}