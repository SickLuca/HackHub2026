package it.unicam.cs.ids.repositories;


import it.unicam.cs.ids.models.Invitation;

import it.unicam.cs.ids.repositories.abstractions.IInvitationRepository;
import jakarta.persistence.EntityManager;

import java.util.List;

public class InvitationRepository implements IInvitationRepository {
    private final EntityManager em;

    public InvitationRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public Invitation create(Invitation invitation) {
        em.getTransaction().begin();
        em.persist(invitation);
        em.getTransaction().commit();
        return invitation;
    }

    @Override
    public Invitation delete(Long id) {
        return null;
    }

    @Override
    public Invitation update(Invitation invitation) {
        em.getTransaction().begin();
        Invitation updated = em.merge(invitation);
        em.getTransaction().commit();
        return updated;
    }

    @Override
    public Invitation getById(Long id) {
        return em.find(Invitation.class, id);
    }

    @Override
    public List<Invitation> getAll() {
        return em.createQuery("SELECT i FROM Invitation i", Invitation.class).getResultList();
    }

}
