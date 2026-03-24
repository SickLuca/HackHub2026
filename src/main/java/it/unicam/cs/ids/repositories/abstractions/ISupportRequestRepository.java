package it.unicam.cs.ids.repositories.abstractions;

import it.unicam.cs.ids.models.SupportRequest;
import java.util.List;

public interface ISupportRequestRepository extends JpaRepository<SupportRequest, Long> {

    // Metodo specifico per il Mentore: recupera le richieste dato l'ID dell'hackathon
    List<SupportRequest> getByHackathonId(Long hackathonId);
}