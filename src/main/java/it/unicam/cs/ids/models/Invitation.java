package it.unicam.cs.ids.models;

import it.unicam.cs.ids.models.utils.InvitationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "invitations")
@Getter
@Setter
@NoArgsConstructor
public class Invitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team fromTeam;

    @ManyToOne
    @JoinColumn(name = "invited_user_id")
    private DefaultUser toUser;

    @Enumerated(EnumType.STRING)
    private InvitationStatus status;

    private LocalDateTime creationDate;


}
