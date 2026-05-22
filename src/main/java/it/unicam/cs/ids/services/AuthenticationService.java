package it.unicam.cs.ids.services;

import it.unicam.cs.ids.dtos.requests.LoginRequestDTO;
import it.unicam.cs.ids.dtos.requests.RegisterRequestDTO;
import it.unicam.cs.ids.dtos.responses.AuthResponseDTO;
import it.unicam.cs.ids.exceptions.InvalidInputException;
import it.unicam.cs.ids.models.DefaultUser;
import it.unicam.cs.ids.models.utils.UserRole;
import it.unicam.cs.ids.security.CustomUserDetails;
import it.unicam.cs.ids.security.JwtService;
import it.unicam.cs.ids.utils.unitOfWork.IUnitOfWork;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service responsible for managing user authentication.
 * <p>
 * Handles registration of new users, login via credentials
 * (email and password), and generation of JSON Web Tokens (JWT) to return
 * to clients for authorizing subsequent requests.
 * </p>
 */
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
            throw new InvalidInputException("A user with this email already exists: " + request.email());

        }

        DefaultUser user = new DefaultUser();
        user.setName(request.name());
        user.setSurname(request.surname());
        user.setEmail(request.email());
        // CRITICAL: We never store the password in plaintext!
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.USER_NO_TEAM); // By default a new user has no team

        unitOfWork.getUserRepository().save(user);

        var jwtToken = jwtService.generateToken(new CustomUserDetails(user));
        return new AuthResponseDTO(jwtToken);
    }

    public AuthResponseDTO authenticate(LoginRequestDTO request) {
        // This method throws an exception if the credentials are incorrect
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // If we reach here, the user exists and the password is correct
        var user = unitOfWork.getUserRepository().findByEmail(request.email()).orElseThrow();
        var jwtToken = jwtService.generateToken(new CustomUserDetails(user));

        return new AuthResponseDTO(jwtToken);
    }
}