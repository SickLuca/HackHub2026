package it.unicam.cs.ids.models;

import it.unicam.cs.ids.models.utils.ReportStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Il mentore che effettua la segnalazione
    @ManyToOne
    @JoinColumn(name = "mentor_id", nullable = false)
    private StaffUser mentor;

    // Il team che ha commesso la violazione
    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    // L'hackathon in cui è avvenuta
    @ManyToOne
    @JoinColumn(name = "hackathon_id", nullable = false)
    private Hackathon hackathon;

    // Descrizione della violazione
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private String decisionNote;
}