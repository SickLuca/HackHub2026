package it.unicam.cs.ids.utils.adapter;

import org.springframework.stereotype.Service;

@Service // Anche lui è un bean, ma senza @Primary Spring lo ignora a meno di richieste esplicite
public class GoogleCalendarAdapter implements ICalendarService {

    private final ExternalGoogleApi googleApi;

    public GoogleCalendarAdapter(ExternalGoogleApi googleApi) {
        this.googleApi = googleApi;
    }

    @Override
    public String generateMeetingLink(String mentorName, String teamName) {
        // Google Meet vuole formati diversi! L'Adapter gestisce questa differenza.
        String organizerEmail = mentorName.trim().toLowerCase() + "@hackhub.com";
        String roomName = teamName.trim().toLowerCase();

        return googleApi.scheduleGoogleMeet(organizerEmail, roomName);
    }
}