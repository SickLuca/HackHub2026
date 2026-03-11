package it.unicam.cs.ids.models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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
    private List<DefaultUser> members = new ArrayList<>();;

    @ManyToOne
    @JoinColumn(name = "hackathon_id")
    private Hackathon subscribedHackathon;

    //TODO controllare che non sballa nulla
    @OneToMany(mappedBy = "team")
    private List<Submission> submissions = new ArrayList<>();;

}