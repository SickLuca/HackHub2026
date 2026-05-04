package it.unicam.cs.ids.security;

import it.unicam.cs.ids.models.abstractions.User;
import it.unicam.cs.ids.repositories.IUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Servizio per il caricamento dei dettagli dell'utente durante l'autenticazione.
 * <p>
 * Implementa l'interfaccia {@link UserDetailsService} di Spring Security per 
 * recuperare l'utente dal database tramite la sua email (utilizzata come username).
 * Ritorna un'istanza di {@link CustomUserDetails}.
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
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato con email: " + email));

        return new CustomUserDetails(user);
    }
}