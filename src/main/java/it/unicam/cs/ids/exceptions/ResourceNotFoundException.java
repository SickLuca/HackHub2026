package it.unicam.cs.ids.exceptions;

// Eccezione per risorse non trovate (es. Hackathon, Team, Utente non trovato)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}