package it.unicam.cs.ids.models;

import it.unicam.cs.ids.models.abstractions.User;
import it.unicam.cs.ids.models.utils.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Entità che rappresenta un utente partecipante all'hackathon.
 * <p>
 * Estende l'entità astratta {@link User} e contiene le informazioni
 * specifiche dei partecipanti, come il ruolo (es. {@code TEAM_LEADER}, 
 * {@code TEAM_MEMBER}), il team di appartenenza e gli inviti ricevuti.
 * </p>
 */
@Entity
@Table(name = "default_users")
@Getter
@Setter
@NoArgsConstructor
public class DefaultUser extends User {

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @OneToMany(mappedBy = "toUser")
    private List<Invitation> invitations = new ArrayList<>();

    public DefaultUser(Long id, UserRole role) {
        super(id);
        this.role = role;
    }


    public DefaultUser(Long id) {
        super(id);
    }
}
