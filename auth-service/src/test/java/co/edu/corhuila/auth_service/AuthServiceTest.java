package co.edu.corhuila.auth_service;

import co.edu.corhuila.auth_service.Repository.UsuarioRepository;
import co.edu.corhuila.auth_service.Service.AuthService;
import co.edu.corhuila.auth_service.Service.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void deberiaLanzarErrorSiUsuarioNoExiste() {
        assertThrows(RuntimeException.class, () ->
                authService.login("noexiste@correo.com", "123456"));
    }
}