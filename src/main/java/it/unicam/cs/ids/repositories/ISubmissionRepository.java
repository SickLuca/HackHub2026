package it.unicam.cs.ids.repositories;

import it.unicam.cs.ids.models.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ISubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByHackathonId(Long hackathonId);
}