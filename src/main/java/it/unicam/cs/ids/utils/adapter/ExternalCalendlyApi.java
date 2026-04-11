package it.unicam.cs.ids.utils.adapter;

import org.springframework.stereotype.Component;

@Component // Lo rendiamo un bean di Spring
public class ExternalCalendlyApi {
    // Metodo proprietario di Calendly (nome diverso, logica diversa)
    public String createCalendlyEvent(String userSlug, String eventName) {
        return String.format("https://calendly.com/%s/%s", userSlug, eventName);
    }
}