package it.unicam.cs.ids.utils.adapter;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Adapter concreto per l'integrazione con Calendly.
 * <p>
 * Implementa {@link ICalendarService} traducendo i dati del dominio nel
 * formato supportato dall'{@link ExternalCalendlyApi}.
 * È contrassegnato con {@link Primary} per indicare a Spring di utilizzarlo
 * come implementazione di default per l'interfaccia.
 * </p>
 */
@Service
@Primary // Dice a Spring che questo è l'Adapter di default da usare
public class CalendlyCalendarAdapter implements ICalendarService {

    private final ExternalCalendlyApi calendlyApi;

    public CalendlyCalendarAdapter(ExternalCalendlyApi calendlyApi) {
        this.calendlyApi = calendlyApi;
    }

    @Override
    public String generateMeetingLink(String mentorName, String teamName) {
        // L'adapter traduce i dati per renderli compatibili con l'API esterna
        String userSlug = mentorName.trim().replaceAll("\\s+", "").toLowerCase();
        String eventName = "support-call-" + teamName.trim().replaceAll("\\s+", "-").toLowerCase();

        return calendlyApi.createCalendlyEvent(userSlug, eventName);
    }
}