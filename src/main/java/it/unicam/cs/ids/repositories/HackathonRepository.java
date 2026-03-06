package it.unicam.cs.ids.repositories;

import it.unicam.cs.ids.models.Hackathon;
import it.unicam.cs.ids.repositories.abstractions.IHackathonRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.ArrayList;
import java.util.List;

public class HackathonRepository implements IHackathonRepository {

    private final EntityManager em;

    // Iniettiamo l'EntityManager
    public HackathonRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public Hackathon create(Hackathon hackathon) {
        // Iniziamo una transazione
        em.getTransaction().begin();

        // Salviamo l'oggetto nel database. JPA popolerà automaticamente l'ID!
        em.persist(hackathon);

        // Confermiamo le modifiche
        em.getTransaction().commit();

        return hackathon;
    }

    @Override
    public Hackathon delete(Long id) {
        return null;
    }

    @Override
    public Hackathon update(Hackathon hackathon) {
        return null;
    }

    @Override
    public Hackathon getById(Long id) {
        // em.find cerca nel database il record con quell'ID e lo trasforma in un oggetto Hackathon
        return em.find(Hackathon.class, id);
    }

    @Override
    public List<Hackathon> getAll() {
        return List.of();
    }

}
