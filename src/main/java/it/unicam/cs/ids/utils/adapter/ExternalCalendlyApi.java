package it.unicam.cs.ids.utils.adapter;

import org.springframework.stereotype.Component;

/**
 * Simulates an external Calendly API service for creating meeting events.
 */
@Component // Registered as a Spring bean
public class ExternalCalendlyApi {
    
    /**
     * Creates a Calendly event link based on the user slug and event name.
     *
     * @param userSlug the Calendly user identifier
     * @param eventName the name of the event
     * @return a formatted Calendly event URL
     */
    // Calendly's proprietary method (different name, different logic)
    public String createCalendlyEvent(String userSlug, String eventName) {
        return String.format("https://calendly.com/%s/%s", userSlug, eventName);
    }
}