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
 * Custom implementation of Spring Security's {@link UserDetails}.
 * <p>
 * Acts as an adapter (wrapper) around the domain {@link User} entity,
 * providing Spring Security with the information required for authentication
 * and authorization (e.g., credentials, account status, and roles/authorities).
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
        // Extract the role based on the concrete type of User
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
        return user.getEmail(); // In Spring Security the "username" is typically the email
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