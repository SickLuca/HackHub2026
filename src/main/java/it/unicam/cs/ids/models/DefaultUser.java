package it.unicam.cs.ids.models;

import it.unicam.cs.ids.models.abstractions.User;
import it.unicam.cs.ids.models.utils.UserRole;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "default_users")
@Getter
@Setter
@NoArgsConstructor
public class DefaultUser extends User {

    private UserRole role;

    private Team team;

    DefaultUser(Long id, UserRole role) {
        super(id);
        this.role = role;
    }

}
