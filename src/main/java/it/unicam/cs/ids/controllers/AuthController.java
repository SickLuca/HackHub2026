package it.unicam.cs.ids.controllers;

import it.unicam.cs.ids.dtos.requests.LoginRequestDTO;
import it.unicam.cs.ids.dtos.requests.RegisterRequestDTO;
import it.unicam.cs.ids.dtos.responses.AuthResponseDTO;
import it.unicam.cs.ids.services.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST per l'autenticazione degli utenti.
 * <p>
 * Espone gli endpoint pubblici per la registrazione di nuovi utenti
 * e il login con generazione di token JWT.
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
     * Registra un nuovo utente nel sistema.
     *
     * @param request DTO contenente i dati di registrazione (username, password, ruolo)
     * @return {@link AuthResponseDTO} contenente il token JWT generato per l'utente appena registrato
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.ok(service.register(request));
    }

    /**
     * Autentica un utente esistente e restituisce un token JWT.
     *
     * @param request DTO contenente le credenziali di accesso (username, password)
     * @return {@link AuthResponseDTO} contenente il token JWT per le successive chiamate autenticate
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> authenticate(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(service.authenticate(request));
    }
}