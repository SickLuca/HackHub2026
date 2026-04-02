package it.unicam.cs.ids.repositories;

import it.unicam.cs.ids.models.Hackathon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IHackathonRepository extends JpaRepository<Hackathon, Long> {

}
