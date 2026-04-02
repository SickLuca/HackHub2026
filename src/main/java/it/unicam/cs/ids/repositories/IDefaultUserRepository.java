package it.unicam.cs.ids.repositories;

import it.unicam.cs.ids.models.DefaultUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDefaultUserRepository extends JpaRepository<DefaultUser, Long> {
}
