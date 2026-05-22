package it.unicam.cs.ids.utils.adapter;

/**
 * Target interface for the Adapter pattern (calendar management).
 * <p>
 * Defines a common contract for generating meeting links,
 * hiding the complexity and differences of the APIs of various providers
 * (e.g. Google Meet, Calendly) from the rest of the application.
 * </p>
 */
public interface ICalendarService {
    /**
     * Generates a booking link for the external calendar.
     */
    String generateMeetingLink(String mentorName, String teamName);
}