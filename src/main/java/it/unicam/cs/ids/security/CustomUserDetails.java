package it.unicam.cs.ids.security;

import it.unicam.cs.ids.models.DefaultUser;
import it.unicam.cs.ids.models.StaffUser;
import it.unicam.cs.ids.models.abstractions.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Implementazione personalizzata di {@link UserDetails} di Spring Security.
 * <p>
 * Funge da adapter (wrapper) attorno all'entità {@link User} del dominio,
 * fornendo a Spring Security le informazioni necessarie per l'autenticazione
 * e l'autorizzazione (es. credenziali, stato dell'account e ruoli/autorità).
 * </p>
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Estraiamo il ruolo in base al tipo concreto di Utente
        String roleName = "ROLE_USER"; // Default fallback

        if (user instanceof StaffUser staff) {
            roleName = "ROLE_" + staff.getRole().name();
        } else if (user instanceof DefaultUser defaultUser) {
            roleName = "ROLE_" + defaultUser.getRole().name();
        }

        return List.of(new SimpleGrantedAuthority(roleName));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // In Spring Security lo "username" è solitamente l'email
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}