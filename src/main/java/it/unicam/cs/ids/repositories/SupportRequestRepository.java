package it.unicam.cs.ids.repositories;

import it.unicam.cs.ids.models.SupportRequest;
import it.unicam.cs.ids.repositories.abstractions.ISupportRequestRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class SupportRequestRepository implements ISupportRequestRepository {

    private final EntityManager entityManager;

    public SupportRequestRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    @Override
    public SupportRequest create(SupportRequest supportRequest) {
        return null;
    }

    @Override
    public SupportRequest delete(Long id) {
        return null;
    }

    @Override
    public SupportRequest update(SupportRequest supportRequest) {
        return null;
    }

    @Override
    public SupportRequest getById(Long id) {
        return null;
    }

    @Override
    public List<SupportRequest> getAll() {
        return List.of();
    }

    @Override
    public List<SupportRequest> getByHackathonId(Long hackathonId) {
        String jpql = "SELECT s FROM SupportRequest s WHERE s.hackathon.id = :hackathonId ORDER BY s.createdAt DESC";
        TypedQuery<SupportRequest> query = entityManager.createQuery(jpql, SupportRequest.class);
        query.setParameter("hackathonId", hackathonId);
        return query.getResultList();
    }
}