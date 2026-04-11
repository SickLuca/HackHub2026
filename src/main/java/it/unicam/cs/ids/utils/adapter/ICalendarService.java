package it.unicam.cs.ids.utils.adapter;

public interface ICalendarService {
    /**
     * Genera un link di prenotazione per il calendario esterno.
     */
    String generateMeetingLink(String mentorName, String teamName);
}