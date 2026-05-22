package it.unicam.cs.ids.models;

import it.unicam.cs.ids.models.utils.HackathonStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//TODO: JPA, to be done before other TODOs

/**
 * Entity representing a Hackathon within the system.
 * <p>
 * Stores all the information about an event, including registration
 * and submission dates, the prize, the regulation, and the associations
 * with staff (organizer, mentors, judge) and participating teams.
 * </p>
 */
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

    private LocalDateTime registrationDeadline;

    private LocalDateTime submitDeadline;

    @Column(columnDefinition = "TEXT") // Useful for long text fields
    private String regulation;

    private Double cashPrize;

    private String location;

    private Integer maxDimensionOfTeam;

    @Enumerated(EnumType.STRING)
    private HackathonStatus status;

    // Many Hackathons can share the same Organizer
    @ManyToOne
    @JoinColumn(name = "organizer_id")
    private StaffUser organizer;

    // Many Hackathons can share the same Judge
    @ManyToOne
    @JoinColumn(name = "judge_id")
    private StaffUser judge;

    // A Hackathon has many Mentors, and a Mentor can participate in many Hackathons
    @ManyToMany
    @JoinTable(
            name = "hackathon_mentors", // Join table linking hackathons and mentors
            joinColumns = @JoinColumn(name = "hackathon_id"),
            inverseJoinColumns = @JoinColumn(name = "mentor_id")
    )

    private List<StaffUser> mentors;



    @OneToMany(mappedBy = "subscribedHackathon")
    private List<Team> teams = new ArrayList<>();

    @OneToMany(mappedBy = "hackathon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Submission> submissions = new ArrayList<>();

    @OneToMany(mappedBy = "hackathon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Report>  reports = new ArrayList<>();

    @OneToMany(mappedBy = "hackathon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupportRequest> supportRequests = new ArrayList<>();

   @OneToOne
    @JoinColumn(name = "winner_id")
    private Team winner;

}
