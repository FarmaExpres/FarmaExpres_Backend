package co.edu.corhuila.auth_service.Controllers;

import co.edu.corhuila.auth_service.DTO.UsuarioRequest;
import co.edu.corhuila.auth_service.DTO.UsuarioResponse;
import co.edu.corhuila.auth_service.Entity.Rol;
import co.edu.corhuila.auth_service.Entity.Usuario;
import co.edu.corhuila.auth_service.Service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioControllersTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioControllers usuarioControllers;

    @Test
    void crearUsuarioDebeRetornar201Created() {
        UsuarioRequest request = mock(UsuarioRequest.class);
        when(request.getNombre()).thenReturn("Admin");
        when(request.getEmail()).thenReturn("admin@farma.com");
        when(request.getPassword()).thenReturn("123456");
        when(request.getRol()).thenReturn("ADMIN");

        Rol rol = new Rol(1, "ADMIN", "Administrador");
        Usuario usuario = new Usuario("Admin", "admin@farma.com", "123456", rol);
        usuario.setId(10L);

        when(usuarioService.crearUsuario(
                eq("Admin"),
                eq("admin@farma.com"),
                eq("123456"),
                eq("ADMIN")
        )).thenReturn(usuario);

        ResponseEntity<UsuarioResponse> response = usuarioControllers.crearUsuario(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(10L, response.getBody().getId());
        assertEquals("ADMIN", response.getBody().getRol());
        verify(usuarioService).crearUsuario("Admin", "admin@farma.com", "123456", "ADMIN");
    }
}
