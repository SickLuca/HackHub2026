package it.unicam.cs.ids.exceptions;

// Exception for resources not found (e.g. Hackathon, Team, User not found)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}