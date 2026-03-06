package it.unicam.cs.ids.models;

import it.unicam.cs.ids.models.utils.HackathonStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

//TODO: Jpa, da fare prima di altri TODO

@Entity
@Table(name = "hackathons")
@Getter
@Setter
@NoArgsConstructor
public class Hackathon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private LocalDateTime registrationDeadline;

    private LocalDateTime submitDeadline;

    @Column(columnDefinition = "TEXT") // Utile per testi lunghi
    private String regulation;

    private Double cashPrize;

    private String location;

    private Integer maxDimensionOfTeam;

    @Enumerated(EnumType.STRING)
    private HackathonStatus status;

    // Molti Hackathon possono avere lo stesso Organizzatore
    @ManyToOne
    @JoinColumn(name = "organizer_id")
    private StaffUser organizer;

    // Molti Hackathon possono avere lo stesso Giudice
    @ManyToOne
    @JoinColumn(name = "judge_id")
    private StaffUser judge;

    // Un Hackathon ha molti Mentori, e un Mentore può partecipare a molti Hackathon
    @ManyToMany
    @JoinTable(
            name = "hackathon_mentors", // Tabella di collegamento (Join Table)
            joinColumns = @JoinColumn(name = "hackathon_id"),
            inverseJoinColumns = @JoinColumn(name = "mentor_id")
    )
    private List<StaffUser> mentors;

    // Per ora diciamo a JPA di ignorare questi campi finché non mapperemo anche queste classi
    @Transient
    private List<Team> teams;

    @Transient
    private List<Submission> submissions; //list<String>?

    @Transient
    private Team vincitore;



}
