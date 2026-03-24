package it.unicam.cs.ids.repositories;

import it.unicam.cs.ids.models.Report;
import it.unicam.cs.ids.repositories.abstractions.IReportRepository;
import jakarta.persistence.EntityManager;

import java.util.List;

public class ReportRepository implements IReportRepository {

    private final EntityManager em;

    public ReportRepository(EntityManager entityManager) {
        this.em = entityManager;
    }

    @Override
    public Report create(Report report) {
        em.getTransaction().begin();
        em.persist(report);
        em.getTransaction().commit();
        return report;
    }

    @Override
    public Report delete(Long id) {
        return null;
    }

    @Override
    public Report update(Report report) {
        return null;
    }

    @Override
    public Report getById(Long id) {
        return null;
    }

    @Override
    public List<Report> getAll() {
        return List.of();
    }

    @Override
    public List<Report> getReportsByHackathonId(Long hackathonId) {
        return em.createQuery("SELECT r FROM Report r WHERE r.hackathon.id = :hackathonId", Report.class)
                .setParameter("hackathonId", hackathonId)
                .getResultList();
    }
}