package it.unicam.cs.ids.exceptions;

// Eccezione per violazioni delle regole di business (es. Team pieno, Scadenza passata)
public class RuleViolationException extends RuntimeException {
    public RuleViolationException(String message) {
        super(message);
    }
}