package it.unicam.cs.ids.utils.adapter;

import org.springframework.stereotype.Component;

/**
 * Simulates an external Google API service for scheduling Google Meet rooms.
 */
@Component // Registered as a Spring bean
public class ExternalGoogleApi {
    
    /**
     * Schedules a Google Meet and generates the meeting room link.
     *
     * @param organizerEmail the email of the meeting organizer
     * @param roomName the specific room or event name
     * @return a formatted Google Meet URL
     */
    // Google's proprietary method (different name, different logic)
    public String scheduleGoogleMeet(String organizerEmail, String roomName) {
        return String.format("https://meet.google.com/%s-room-%s", organizerEmail, roomName);
    }
}