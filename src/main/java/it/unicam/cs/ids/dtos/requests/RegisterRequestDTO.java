package it.unicam.cs.ids.dtos.requests;

public record RegisterRequestDTO(
        String name,
        String surname,
        String email,
        String password) {

}