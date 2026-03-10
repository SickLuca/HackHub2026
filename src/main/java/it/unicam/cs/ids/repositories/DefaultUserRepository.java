package it.unicam.cs.ids.repositories;

import it.unicam.cs.ids.models.DefaultUser;
import it.unicam.cs.ids.repositories.abstractions.IDefaultUserRepository;
import jakarta.persistence.EntityManager;
import java.util.List;

public class DefaultUserRepository implements IDefaultUserRepository {
    private final EntityManager em;

    public DefaultUserRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public DefaultUser create(DefaultUser user) {
        em.getTransaction().begin();
        em.persist(user);
        em.getTransaction().commit();
        return user;
    }

    @Override
    public DefaultUser update(DefaultUser user) {
        // 'merge' aggiorna un'entità già esistente sul database
        em.getTransaction().begin();
        DefaultUser updated = em.merge(user);
        em.getTransaction().commit();
        return updated;
    }

    @Override public DefaultUser delete(Long id) { return null; }
    @Override public DefaultUser getById(Long id) { return em.find(DefaultUser.class, id); }
    @Override public List<DefaultUser> getAll() { return null; }
}