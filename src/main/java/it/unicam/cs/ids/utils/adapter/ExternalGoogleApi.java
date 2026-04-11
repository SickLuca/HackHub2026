package it.unicam.cs.ids.utils.adapter;

import org.springframework.stereotype.Component;

@Component // Lo rendiamo un bean di Spring
public class ExternalGoogleApi {
    // Metodo proprietario di Google (nome diverso, logica diversa)
    public String scheduleGoogleMeet(String organizerEmail, String roomName) {
        return String.format("https://meet.google.com/%s-room-%s", organizerEmail, roomName);
    }
}