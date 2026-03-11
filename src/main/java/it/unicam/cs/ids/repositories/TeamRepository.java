package it.unicam.cs.ids.repositories;

import it.unicam.cs.ids.models.Team;
import it.unicam.cs.ids.repositories.abstractions.ITeamRepository;
import jakarta.persistence.EntityManager;

import java.util.List;

public class TeamRepository implements ITeamRepository {
    private final EntityManager em;

    public TeamRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public Team create(Team team) {
        em.getTransaction().begin();
        em.persist(team);
        em.getTransaction().commit();
        return team;
    }

    @Override public Team delete(Long id) { return null; }

    @Override public Team update(Team team) {
        em.getTransaction().begin();
        Team updated = em.merge(team);
        em.getTransaction().commit();
        return updated; }

    @Override
    public Team getById(Long id) {
        return em.find(Team.class, id);
    }

    @Override
    public List<Team> getAll() {
        return em.createQuery("SELECT t FROM Team t", Team.class).getResultList();
    }
}