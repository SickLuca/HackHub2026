package it.unicam.cs.ids.models.utils;

public enum SupportRequestStatus {
    PENDING,           // Appena creata dal team
    SCHEDULED,    // Quando un mentore la prende in carico o propone una call
    CLOSED        // Quando la richiesta è stata soddisfatta
}