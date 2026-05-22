package it.unicam.cs.ids.models;

import it.unicam.cs.ids.models.utils.ReportStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing a report created by a mentor.
 * <p>
 * Used to flag regulation violations by a team
 * during a hackathon. Contains the report status and any
 * decision notes made by the organizer.
 * </p>
 */
@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The mentor who created the report
    @ManyToOne
    @JoinColumn(name = "mentor_id", nullable = false)
    private StaffUser mentor;

    // The team that committed the violation
    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    // The hackathon in which it occurred
    @ManyToOne
    @JoinColumn(name = "hackathon_id", nullable = false)
    private Hackathon hackathon;

    // Description of the violation
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private String decisionNote;
}