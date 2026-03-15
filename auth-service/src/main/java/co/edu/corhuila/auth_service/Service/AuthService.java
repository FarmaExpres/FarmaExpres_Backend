package co.edu.corhuila.auth_service.Service;


import co.edu.corhuila.auth_service.DTO.LoginResponseDto;
import co.edu.corhuila.auth_service.Entity.Bitacora;
import co.edu.corhuila.auth_service.Entity.EstadoUsuario;
import co.edu.corhuila.auth_service.Entity.Usuario;
import co.edu.corhuila.auth_service.Repository.BitacoraRepository;
import co.edu.corhuila.auth_service.Repository.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final BitacoraRepository bitacoraRepository;


    public AuthService(UsuarioRepository usuarioRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       BitacoraRepository bitacoraRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.bitacoraRepository = bitacoraRepository;

    }

    // =========================
    // Registrar Login
    // =========================

    public LoginResponseDto login(String email, String password) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

        if (usuario == null) {
            registrarLoginFallido(null, email);
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Usuario no encontrado"
            );
        }

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            registrarLoginFallido(usuario.getId(), email);
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Credenciales inválidas"
            );
        }

        if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
            registrarLoginFallido(usuario.getId(), email);
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Usuario no activo"
            );
        }

        registrarLoginExitoso(usuario.getId());

        String token = jwtService.generarToken(
                usuario.getEmail(),
                usuario.getRol().getNombre()
        );

        return new LoginResponseDto(
                token,
                "Bearer",
                usuario.getEmail(),
                usuario.getRol().getNombre()
        );
    }



    // =========================
// Registrar Login Exitoso
// =========================
    private void registrarLoginExitoso(Long usuarioId) {
        bitacoraRepository.save(
                new Bitacora(usuarioId, "LOGIN_EXITOSO")
        );
    }

    // =========================
// Registrar Login Fallido
// =========================
    private void registrarLoginFallido(Long usuarioId, String email) {
        bitacoraRepository.save(
                new Bitacora(usuarioId, "LOGIN_FALLIDO - " + email)
        );
    }

}
