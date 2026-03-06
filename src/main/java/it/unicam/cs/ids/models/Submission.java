package it.unicam.cs.ids.models;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Submission {

    private Long id;

    private Team team;

    private Hackathon hackathon;

    private String projectUrl;

    private String description;

    private LocalDateTime submissionDate;

    private Integer score;

    private String judgeFeedback;

}
