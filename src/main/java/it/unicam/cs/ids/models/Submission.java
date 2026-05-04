package it.unicam.cs.ids.models;

import it.unicam.cs.ids.models.utils.SubmissionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entità che rappresenta la consegna di un progetto (submission) da parte di un team.
 * <p>
 * Contiene i dettagli del progetto presentato, come l'URL del repository o della demo,
 * la descrizione, e i risultati della valutazione (punteggio e feedback del giudice).
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