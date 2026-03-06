package it.unicam.cs.ids.models;

import it.unicam.cs.ids.models.abstractions.User;
import it.unicam.cs.ids.models.utils.StaffRole;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "staff_users")
@Getter
@Setter
@NoArgsConstructor
public class StaffUser extends User {

    @Enumerated(EnumType.STRING)
    private StaffRole role;

    public StaffUser(Long id, StaffRole role) {
        super(id);
        this.role = role;
    }
}
