package it.unicam.cs.ids.repositories;

import it.unicam.cs.ids.models.Submission;
import it.unicam.cs.ids.repositories.abstractions.ISubmissionRepository;
import jakarta.persistence.EntityManager;

import java.util.List;

public class SubmissionRepository implements ISubmissionRepository {
    private final EntityManager em;

    public SubmissionRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public Submission create(Submission submission) {
        em.getTransaction().begin();
        em.persist(submission);
        em.getTransaction().commit();
        return submission;
    }

    @Override
    public Submission delete(Long id) {
        return null;
    }

    @Override
    public Submission update(Submission submission) {
        em.getTransaction().begin();
        Submission updated = em.merge(submission);
        em.getTransaction().commit();
        return updated;
    }

    @Override
    public Submission getById(Long id) {
        return null;
    }

    @Override
    public List<Submission> getAll() {
        return List.of();
    }
}