package it.unicam.cs.ids.models;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Team {

    private Long id;

    private String name;

    private List<DefaultUser> members;

    private Hackathon subscribedHackathon;

}
