package co.edu.corhuila.auth_service.Service;


import co.edu.corhuila.auth_service.DTO.LoginResponseDto;
import co.edu.corhuila.auth_service.Entity.Binnacle;
import co.edu.corhuila.auth_service.Entity.UserStatus;
import co.edu.corhuila.auth_service.Entity.User;
import co.edu.corhuila.auth_service.Repository.BinnacleRepository;
import co.edu.corhuila.auth_service.Repository.UserRepository;
import co.edu.corhuila.auth_service.Validation.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service
public class AuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
    private static final String INVALID_CREDENTIALS_MESSAGE = "Credenciales incorrectas";

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final BinnacleRepository binnacleRepository;


    public AuthService(UserRepository userRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       BinnacleRepository binnacleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.binnacleRepository = binnacleRepository;

    }

    // =========================
    // Registrar Login
    // =========================

    public LoginResponseDto login(String email, String password) {
        EmailValidator.validateOrThrow(email);

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            registerLoginFailed(null, email);
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    INVALID_CREDENTIALS_MESSAGE
            );
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            registerLoginFailed(user.getId(), email);
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    INVALID_CREDENTIALS_MESSAGE
            );
        }

        if (user.getState() != UserStatus.Asset) {
            registerLoginFailed(user.getId(), email);
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Usuario no activo"
            );
        }

        successfullogin(user.getId());

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole().getName(),
                user.getName(),
                user.getId()
        );

        return new LoginResponseDto(
                token,
                "Bearer",
                user.getEmail(),
                user.getRole().getName(),
                user.getName()
        );
    }



    // =========================
// Registrar Login Exitoso
// =========================
    private void successfullogin(Long usuarioId) {
        binnacleRepository.save(
                new Binnacle(usuarioId, "LOGIN_EXITOSO")
        );
    }

    // =========================
// Registrar Login Fallido
// =========================
    private void registerLoginFailed(Long usuarioId, String email) {
        try {
            binnacleRepository.save(
                    new Binnacle(usuarioId, "LOGIN_FALLIDO - " + email)
            );
        } catch (Exception ex) {
            // A failed audit write must not change authentication semantics.
            LOGGER.warn("No se pudo registrar LOGIN_FALLIDO para {}", email, ex);
        }
    }

}
