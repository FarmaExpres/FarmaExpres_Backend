package co.edu.corhuila.auth_service.Service;


import co.edu.corhuila.auth_service.DTO.LoginResponseDto;
import co.edu.corhuila.auth_service.Entity.Binnacle;
import co.edu.corhuila.auth_service.Entity.UserStatus;
import co.edu.corhuila.auth_service.Entity.User;
import co.edu.corhuila.auth_service.Repository.BinnacleRepository;
import co.edu.corhuila.auth_service.Repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service
public class AuthService {

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
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            loginFailed(null, email);
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Usuario no encontrado"
            );
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            loginFailed(user.getId(), email);
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Credenciales inválidas"
            );
        }

        if (user.getState() != UserStatus.Asset) {
            loginFailed(user.getId(), email);
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Usuario no activo"
            );
        }

        successfullogin(user.getId());

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole().getName()
        );

        return new LoginResponseDto(
                token,
                "Bearer",
                user.getEmail(),
                user.getRole().getName()
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
    private void loginFailed(Long usuarioId, String email) {
        binnacleRepository.save(
                new Binnacle(usuarioId, "LOGIN_FALLIDO - " + email)
        );
    }

}
