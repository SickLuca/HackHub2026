package it.unicam.cs.ids.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro personalizzato per l'autenticazione basata su JWT.
 * <p>
 * Estende {@link OncePerRequestFilter} per garantire che venga eseguito
 * una sola volta per ogni richiesta HTTP. Intercetta le richieste,
 * estrae il token JWT dall'header "Authorization", lo valida e, se valido,
 * imposta l'autenticazione nel {@link SecurityContextHolder}.
 * </p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 1. Controlliamo se c'è l'header Authorization e se inizia con "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Estraiamo il token (togliendo i primi 7 caratteri "Bearer ")
        jwt = authHeader.substring(7);

        // 3. Estraiamo l'email dal token
        userEmail = jwtService.extractUsername(jwt);

        // 4. Se abbiamo l'email e l'utente non è ancora autenticato nel contesto di Spring...
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Carichiamo l'utente dal database
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 5. Se il token è valido, creiamo l'oggetto di autenticazione e lo diamo a Spring
            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Salviamo l'autenticazione nel contesto. Da questo momento la richiesta è "Autorizzata"
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Passiamo la palla al prossimo filtro (o al Controller)
        filterChain.doFilter(request, response);
    }
}