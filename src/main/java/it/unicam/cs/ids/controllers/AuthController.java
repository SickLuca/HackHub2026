package it.unicam.cs.ids.controllers;

import it.unicam.cs.ids.dtos.requests.LoginRequestDTO;
import it.unicam.cs.ids.dtos.requests.RegisterRequestDTO;
import it.unicam.cs.ids.dtos.responses.AuthResponseDTO;
import it.unicam.cs.ids.services.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService service;

    public AuthController(AuthenticationService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> authenticate(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(service.authenticate(request));
    }
}