package it.unicam.cs.ids.repositories.abstractions;

import it.unicam.cs.ids.models.Submission;
import java.util.List;

public interface ISubmissionRepository extends JpaRepository<Submission, Long>{
    List<Submission> getByHackathon(Long hackathonId);
}