package it.unicam.cs.ids.security;

import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for interacting with the security context.
 * <p>
 * Provides helper methods to quickly access information
 * about the currently authenticated user without having to query
 * the {@link SecurityContextHolder} explicitly in every service or controller.
 * </p>
 */
public class SecurityUtils {

    /**
     * Extracts the ID of the currently authenticated user from the Spring Security context.
     * @return Long id
     */
    public static Long getAuthenticatedUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUser().getId();
        }

        throw new SecurityException("User not authenticated or invalid token");
    }
}