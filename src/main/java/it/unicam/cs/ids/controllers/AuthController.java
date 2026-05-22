package it.unicam.cs.ids.controllers;

import it.unicam.cs.ids.dtos.requests.LoginRequestDTO;
import it.unicam.cs.ids.dtos.requests.RegisterRequestDTO;
import it.unicam.cs.ids.dtos.responses.AuthResponseDTO;
import it.unicam.cs.ids.services.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user authentication.
 * <p>
 * Exposes public endpoints for registering new users
 * and logging in with JWT token generation.
 * </p>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService service;

    public AuthController(AuthenticationService service) {
        this.service = service;
    }

    /**
     * Registers a new user in the system.
     *
     * @param request DTO containing the registration data (username, password, role)
     * @return {@link AuthResponseDTO} containing the JWT token generated for the newly registered user
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.ok(service.register(request));
    }

    /**
     * Authenticates an existing user and returns a JWT token.
     *
     * @param request DTO containing the login credentials (username, password)
     * @return {@link AuthResponseDTO} containing the JWT token for subsequent authenticated requests
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> authenticate(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(service.authenticate(request));
    }
}