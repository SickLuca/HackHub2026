package it.unicam.cs.ids.models;

import it.unicam.cs.ids.models.utils.SubmissionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing a team's project delivery (submission).
 * <p>
 * Contains the details of the submitted project, such as the repository or demo URL,
 * the description, and the evaluation results (score and judge feedback).
 * </p>
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne
    @JoinColumn(name = "hackathon_id")
    private Hackathon hackathon;

    private String projectUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDateTime submissionDate;

    private Integer score;

    @Column(columnDefinition = "TEXT")
    private String judgeFeedback;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status = SubmissionStatus.OPEN;

}