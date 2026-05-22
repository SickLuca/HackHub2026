package it.unicam.cs.ids.security;

import it.unicam.cs.ids.models.abstractions.User;
import it.unicam.cs.ids.repositories.IUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service for loading user details during authentication.
 * <p>
 * Implements the {@link UserDetailsService} interface from Spring Security to
 * retrieve the user from the database by their email (used as the username).
 * Returns an instance of {@link CustomUserDetails}.
 * </p>
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final IUserRepository userRepository;

    public CustomUserDetailsService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new CustomUserDetails(user);
    }
}