package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.requests.LoginRequestDTO;
import it.unicam.cs.ids.dtos.requests.RegisterRequestDTO;
import it.unicam.cs.ids.dtos.responses.AuthResponseDTO;
import it.unicam.cs.ids.models.DefaultUser;
import it.unicam.cs.ids.models.utils.UserRole;
import it.unicam.cs.ids.security.CustomUserDetails;
import it.unicam.cs.ids.security.JwtService;
import it.unicam.cs.ids.utils.unitOfWork.IUnitOfWork;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final IUnitOfWork unitOfWork;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(IUnitOfWork unitOfWork, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.unitOfWork = unitOfWork;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponseDTO register(RegisterRequestDTO request) {

        boolean exists = unitOfWork.getUserRepository().findByEmail(request.email()).isPresent();
        if (exists) {
            throw new IllegalArgumentException("Esiste già un utente con questa email: " + request.email());

        }

        DefaultUser user = new DefaultUser();
        user.setName(request.name());
        user.setSurname(request.surname());
        user.setEmail(request.email());
        // CRITICO: Non salviamo mai la password in chiaro!
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.USER_NO_TEAM); // Di default un nuovo utente non ha team

        unitOfWork.getUserRepository().save(user);

        var jwtToken = jwtService.generateToken(new CustomUserDetails(user));
        return new AuthResponseDTO(jwtToken);
    }

    public AuthResponseDTO authenticate(LoginRequestDTO request) {
        // Questo metodo lancia un'eccezione se le credenziali sono errate
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // Se arriviamo qui, l'utente esiste e la password è corretta
        var user = unitOfWork.getUserRepository().findByEmail(request.email()).orElseThrow();
        var jwtToken = jwtService.generateToken(new CustomUserDetails(user));

        return new AuthResponseDTO(jwtToken);
    }
}