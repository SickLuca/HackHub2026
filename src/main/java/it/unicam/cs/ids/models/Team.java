package it.unicam.cs.ids.models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Entità che rappresenta un team partecipante a un hackathon.
 * <p>
 * Aggrega più utenti partecipanti ({@link DefaultUser}), è associato a un
 * singolo hackathon e gestisce il bilancio (es. per eventuali premi vinti),
 * le submission, le richieste di supporto e i report ricevuti.
 * </p>
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @OneToMany(mappedBy = "team")
    private List<DefaultUser> members = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "hackathon_id")
    private Hackathon subscribedHackathon;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Submission> submissions = new ArrayList<>();

    @OneToMany(mappedBy = "fromTeam", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Invitation>  invitations = new ArrayList<>();

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupportRequest> supportRequests = new ArrayList<>();

    @Column(nullable = false)
    private Double balance = 0.0;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Report> reports = new ArrayList<>();
}