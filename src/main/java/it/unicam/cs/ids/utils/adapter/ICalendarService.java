package it.unicam.cs.ids.utils.adapter;

/**
 * Interfaccia target per il pattern Adapter (gestione calendari).
 * <p>
 * Definisce un contratto comune per la generazione di link ai meeting,
 * nascondendo la complessità e le differenze delle API dei vari provider 
 * (es. Google Meet, Calendly) dal resto dell'applicazione.
 * </p>
 */
public interface ICalendarService {
    /**
     * Genera un link di prenotazione per il calendario esterno.
     */
    String generateMeetingLink(String mentorName, String teamName);
}