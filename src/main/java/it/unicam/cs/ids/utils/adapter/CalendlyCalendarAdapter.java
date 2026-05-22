package it.unicam.cs.ids.utils.adapter;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Concrete Adapter for Calendly integration.
 * <p>
 * Implements {@link ICalendarService} by translating domain data into the
 * format supported by the {@link ExternalCalendlyApi}.
 * It is annotated with {@link Primary} to instruct Spring to use it
 * as the default implementation for the interface.
 * </p>
 */
@Service
@Primary // Tells Spring that this is the default Adapter to use
public class CalendlyCalendarAdapter implements ICalendarService {

    private final ExternalCalendlyApi calendlyApi;

    public CalendlyCalendarAdapter(ExternalCalendlyApi calendlyApi) {
        this.calendlyApi = calendlyApi;
    }

    @Override
    public String generateMeetingLink(String mentorName, String teamName) {
        // The adapter translates the data to make it compatible with the external API
        String userSlug = mentorName.trim().replaceAll("\\s+", "").toLowerCase();
        String eventName = "support-call-" + teamName.trim().replaceAll("\\s+", "-").toLowerCase();

        return calendlyApi.createCalendlyEvent(userSlug, eventName);
    }
}