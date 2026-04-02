package it.unicam.cs.ids.repositories;

import it.unicam.cs.ids.models.SupportRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ISupportRequestRepository extends JpaRepository<SupportRequest, Long> {

    // Metodo specifico per il Mentore: recupera le richieste dato l'ID dell'hackathon
    List<SupportRequest> findByHackathonId(Long hackathonId);
}