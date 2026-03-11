package it.unicam.cs.ids.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Non deve essere specificato necessariamente nel team la bidirezionalità ma è buona norma
    //Il team contiene eventualmente una List<Submission> che è riferita a Submissions di hackathons diversi
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

}