package it.unicam.cs.ids.repositories;

import it.unicam.cs.ids.models.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IInvitationRepository extends JpaRepository<Invitation, Long> {
}
