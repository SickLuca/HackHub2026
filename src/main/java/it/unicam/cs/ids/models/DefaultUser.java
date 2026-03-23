package it.unicam.cs.ids.models;

import it.unicam.cs.ids.models.abstractions.User;
import it.unicam.cs.ids.models.utils.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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

    DefaultUser(Long id, UserRole role) {
        super(id);
        this.role = role;
    }


}
