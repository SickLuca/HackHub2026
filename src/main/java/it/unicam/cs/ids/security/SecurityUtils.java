package it.unicam.cs.ids.security;

import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Classe di utilità per interagire con il contesto di sicurezza.
 * <p>
 * Fornisce metodi helper per accedere rapidamente alle informazioni
 * sull'utente attualmente autenticato senza dover interrogare esplicitamente
 * il {@link SecurityContextHolder} in ogni servizio o controller.
 * </p>
 */
public class SecurityUtils {

    /**
     * Estrae l'ID dell'utente attualmente autenticato dal contesto di Spring Security.
     * @return Long id
     */
    public static Long getAuthenticatedUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUser().getId();
        }

        throw new SecurityException("Utente non autenticato o token non valido");
    }
}