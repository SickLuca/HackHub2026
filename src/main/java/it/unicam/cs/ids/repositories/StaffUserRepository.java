package it.unicam.cs.ids.repositories;

import it.unicam.cs.ids.models.StaffUser;
import it.unicam.cs.ids.repositories.abstractions.IStaffUserRepository;
import jakarta.persistence.EntityManager;

import java.util.List;

public class StaffUserRepository implements IStaffUserRepository {

    private final EntityManager em;

    public StaffUserRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public StaffUser create(StaffUser user) {
        em.getTransaction().begin();
        em.persist(user);
        em.getTransaction().commit();
        return user;
    }

    @Override
    public StaffUser delete(Long id) {
        return null;
    }

    @Override
    public StaffUser update(StaffUser user) {
        return null;
    }

    @Override
    public StaffUser getById(Long id) {
        // em.find cerca nel database il record con quell'ID e lo trasforma in un oggetto StaffUser
        return em.find(StaffUser.class, id);
    }

    @Override
    public List<StaffUser> getAll() {
        return null;
    }
}