package it.unicam.cs.ids.repositories.abstractions;

import it.unicam.cs.ids.models.Report;
import java.util.List;

public interface IReportRepository extends JpaRepository<Report, Long>{
    List<Report> getReportsByHackathonId(Long hackathonId);
}