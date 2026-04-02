package it.unicam.cs.ids.repositories;

import it.unicam.cs.ids.models.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface IReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByHackathonId(Long hackathonId);
}