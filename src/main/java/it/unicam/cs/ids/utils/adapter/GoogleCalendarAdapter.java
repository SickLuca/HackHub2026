package it.unicam.cs.ids.utils.adapter;

import org.springframework.stereotype.Service;

/**
 * Adapter class that implements the {@link ICalendarService} interface
 * to interact with the external Google Calendar/Meet API.
 */
@Service // Also a bean, but without @Primary Spring will ignore it unless explicitly requested
public class GoogleCalendarAdapter implements ICalendarService {

    private final ExternalGoogleApi googleApi;

    /**
     * Constructs a new {@code GoogleCalendarAdapter} with the specified external Google API.
     *
     * @param googleApi the external Google API service to use
     */
    public GoogleCalendarAdapter(ExternalGoogleApi googleApi) {
        this.googleApi = googleApi;
    }

    /**
     * Generates a meeting link using the external Google API.
     * Maps the internal format (mentor and team names) to the Google API specific format.
     *
     * @param mentorName the name of the mentor organizing the meeting
     * @param teamName the name of the team attending the meeting
     * @return the generated Google Meet URL
     */
    @Override
    public String generateMeetingLink(String mentorName, String teamName) {
        // Google Meet requires different formats! The Adapter handles this difference.
        String organizerEmail = mentorName.trim().toLowerCase() + "@hackhub.com";
        String roomName = teamName.trim().toLowerCase();

        return googleApi.scheduleGoogleMeet(organizerEmail, roomName);
    }
}