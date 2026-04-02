package it.unicam.cs.ids.repositories;

import it.unicam.cs.ids.models.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ITeamRepository extends JpaRepository<Team, Long> {

}
